package searchengine.indexer;

import searchengine.crawler.CrawlResult;
import searchengine.crawler.CrawlStats;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.repository.FileIndexRepository;
import searchengine.indexer.strategy.FileIndexingStrategy;
import searchengine.extractor.TextExtractor;
import searchengine.ranking.PathScore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class Indexer {

    private final RecursiveFileCrawler crawler;
    private final FileIndexRepository repository;
    private final List<FileIndexingStrategy> strategies;
    private static final int TRANSACTION_BATCH_SIZE = 100;

    public Indexer(
            RecursiveFileCrawler crawler,
            FileIndexRepository repository,
            List<FileIndexingStrategy> strategies
    ) {
        this.crawler = crawler;
        this.repository = repository;
        this.strategies = strategies;
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
                        Optional<FileIndexingStrategy> strategy = findStrategy(file);

                        if (strategy.isEmpty()) {
                            indexingStats.incrementErrors();
                            continue;
                        }

                        IndexedFileData fileData = strategy.get()
                                .buildIndexedFileData(rootPath, file, currentModifiedAt);

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

    private Optional<FileIndexingStrategy> findStrategy(Path file) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(file))
                .findFirst();
    }


}