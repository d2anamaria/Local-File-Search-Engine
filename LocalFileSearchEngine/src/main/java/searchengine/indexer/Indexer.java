package searchengine.indexer;

import searchengine.crawler.CrawlResult;
import searchengine.crawler.CrawlStats;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.FileIndexRepository;
import searchengine.extractor.TextExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Indexer {

    private final RecursiveFileCrawler crawler;
    private final TextExtractor extractor;
    private final FileIndexRepository repository;

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
        if (listener != null) {
            listener.onCrawlingStarted();
        }

        CrawlResult crawlResult = crawler.crawl(rootPath);
        CrawlStats crawlStats = crawlResult.getStats();

        int totalFiles = crawlResult.getDiscoveredFiles().size();

        if (listener != null) {
            listener.onCrawlingFinished(totalFiles);
        }

        IndexingStats indexingStats = new IndexingStats();

        try {
            String rootAbsolutePath = rootPath.toAbsolutePath().toString();
            Map<String, String> indexedFilesByPath =
                    repository.findIndexedModifiedTimesUnderRoot(rootAbsolutePath);

            Set<String> currentPaths = new HashSet<>();
            int current = 0;

            for (Path file : crawlResult.getDiscoveredFiles()) {
                current++;

                try {
                    String absolutePath = file.toAbsolutePath().toString();
                    String currentModifiedAt = Files.getLastModifiedTime(file).toInstant().toString();

                    currentPaths.add(absolutePath);

                    String indexedModifiedAt = indexedFilesByPath.get(absolutePath);

                    if (indexedModifiedAt != null && indexedModifiedAt.equals(currentModifiedAt)) {
                        indexingStats.incrementFilesUnchanged();
                    } else {
                        IndexedFileData fileData = buildIndexedFileData(file, currentModifiedAt);
                        repository.save(fileData);
                        indexingStats.incrementFilesIndexed();
                    }
                } catch (Exception e) {
                    indexingStats.incrementErrors();
                }

                if (listener != null) {
                    listener.onIndexingProgress(current, totalFiles);
                }
            }

            for (String indexedPath : indexedFilesByPath.keySet()) {
                if (!currentPaths.contains(indexedPath)) {
                    try {
                        repository.deleteByPath(indexedPath);
                        indexingStats.incrementFilesDeletedFromIndex();
                    } catch (Exception e) {
                        indexingStats.incrementErrors();
                    }
                }
            }

        } catch (Exception e) {
            indexingStats.incrementErrors();
        }

        return new IndexingResult(crawlStats, indexingStats);
    }

    private IndexedFileData buildIndexedFileData(Path file, String modifiedAt) throws Exception {
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
                content
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