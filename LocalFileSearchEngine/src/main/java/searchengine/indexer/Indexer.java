package searchengine.indexer;

import searchengine.crawler.CrawlResult;
import searchengine.crawler.CrawlStats;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.FileIndexRepository;
import searchengine.extractor.TextExtractor;
import searchengine.ranking.PathScore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class Indexer {

    private final RecursiveFileCrawler crawler;
    private final TextExtractor extractor;
    private final FileIndexRepository repository;
    private static final int TRANSACTION_BATCH_SIZE = 100;

    public Indexer(
            RecursiveFileCrawler crawler,
            TextExtractor extractor,
            FileIndexRepository repository
    ) {
        this.crawler = crawler;
        this.extractor = extractor;
        this.repository = repository;
    }

    public IndexingResult index(Path rootPath, IndexingProgressListener listener) {
        return index(rootPath, listener, () -> false);
    }

    public IndexingResult index(
            Path rootPath,
            IndexingProgressListener listener,
            BooleanSupplier stopRequested
    ) {
        long totalStart = System.nanoTime();

        if (listener != null) {
            listener.onCrawlingStarted();
        }

        CrawlResult crawlResult = crawler.crawl(rootPath, stopRequested);
        CrawlStats crawlStats = crawlResult.getStats();

        int totalFiles = crawlResult.getDiscoveredFiles().size();

        if (listener != null) {
            listener.onCrawlingFinished(totalFiles);
        }

        long indexingStart = System.nanoTime();

        IndexingStats indexingStats = new IndexingStats();
        int pendingDbOperations = 0;
        boolean cancelled = stopRequested.getAsBoolean();

        try {
            String rootAbsolutePath = rootPath.toAbsolutePath().toString();
            Map<String, String> indexedFilesByPath =
                    repository.findIndexedModifiedTimesUnderRoot(rootAbsolutePath);

            Set<String> currentPaths = new HashSet<>();
            int current = 0;

            repository.beginTransaction();

            for (Path file : crawlResult.getDiscoveredFiles()) {
                if (stopRequested.getAsBoolean()) {
                    cancelled = true;
                    break;
                }

                current++;

                try {
                    String absolutePath = file.toAbsolutePath().toString();
                    String currentModifiedAt = Files.getLastModifiedTime(file).toInstant().toString();

                    currentPaths.add(absolutePath);

                    String indexedModifiedAt = indexedFilesByPath.get(absolutePath);

                    if (indexedModifiedAt != null && indexedModifiedAt.equals(currentModifiedAt)) {
                        indexingStats.incrementFilesUnchanged();
                    } else {
                        IndexedFileData fileData = buildIndexedFileData(rootPath, file, currentModifiedAt);
                        repository.save(fileData);
                        indexingStats.incrementFilesIndexed();

                        pendingDbOperations++;
                        if (pendingDbOperations >= TRANSACTION_BATCH_SIZE) {
                            repository.commitTransaction();
                            pendingDbOperations = 0;
                        }
                    }
                } catch (Exception e) {
                    indexingStats.incrementErrors();
                }

                if (listener != null) {
                    listener.onIndexingProgress(current, totalFiles);
                }
            }

            if (!cancelled) {
                for (String indexedPath : indexedFilesByPath.keySet()) {
                    if (stopRequested.getAsBoolean()) {
                        cancelled = true;
                        break;
                    }

                    if (!currentPaths.contains(indexedPath)) {
                        try {
                            repository.deleteByPath(indexedPath);
                            indexingStats.incrementFilesDeletedFromIndex();

                            pendingDbOperations++;
                            if (pendingDbOperations >= TRANSACTION_BATCH_SIZE) {
                                repository.commitTransaction();
                                pendingDbOperations = 0;
                            }
                        } catch (Exception e) {
                            indexingStats.incrementErrors();
                        }
                    }
                }
            }

            repository.commitTransaction();

        } catch (Exception e) {
            indexingStats.incrementErrors();
            repository.rollbackTransaction();
        } finally {
            try {
                repository.endTransaction();
            } catch (Exception ignored) {
            }
        }

        long totalDurationMillis = (System.nanoTime() - totalStart) / 1_000_000;
        long indexingDurationMillis = (System.nanoTime() - indexingStart) / 1_000_000;

        return new IndexingResult(
                crawlStats,
                indexingStats,
                totalDurationMillis,
                indexingDurationMillis,
                cancelled
        );
    }

    private IndexedFileData buildIndexedFileData(Path rootPath, Path file, String modifiedAt) throws Exception {
        String absolutePath = file.toAbsolutePath().toString();
        String fileName = file.getFileName().toString();
        String content = extractor.extractText(file);
        String preview = extractor.preview(content);

        String extension = getExtension(fileName);
        String mimeType = Files.probeContentType(file);
        long sizeBytes = Files.size(file);
        String indexedAt = Instant.now().toString();
        boolean isHidden = Files.isHidden(file);
        boolean isTextFile = content != null && !content.isBlank();

        PathScore pathScore = new PathScore(rootPath, file, extension, sizeBytes);

        return new IndexedFileData(
                absolutePath,
                fileName,
                extension,
                mimeType,
                sizeBytes,
                null,
                modifiedAt,
                indexedAt,
                null,
                isHidden,
                isTextFile,
                preview,
                content,
                pathScore.getPathDepth(),
                pathScore.getDirectoryScore(),
                pathScore.getExtensionScore(),
                pathScore.getSizeScore(),
                pathScore.getPathScore()
        );
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}