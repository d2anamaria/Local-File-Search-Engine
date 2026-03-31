package searchengine.crawler;

import searchengine.config.IndexingRules;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;

public class RecursiveFileCrawler {

    private final IndexingRules indexingRules;

    public RecursiveFileCrawler(IndexingRules indexingRules) {
        this.indexingRules = indexingRules;
    }

    public CrawlResult crawl(Path rootPath) {
        return crawl(rootPath, () -> false);
    }

    public CrawlResult crawl(Path rootPath, BooleanSupplier stopRequested) {
        List<Path> discoveredFiles = new ArrayList<>();
        CrawlStats stats = new CrawlStats();

        if (rootPath == null || !Files.exists(rootPath)) {
            System.err.println("Root path does not exist: " + rootPath);
            stats.incrementErrors();
            return new CrawlResult(discoveredFiles, stats);
        }

        try {
            Files.walkFileTree(
                    rootPath,
                    EnumSet.noneOf(FileVisitOption.class),
                    Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            if (stopRequested.getAsBoolean()) {
                                return FileVisitResult.TERMINATE;
                            }

                            try {
                                if (indexingRules.shouldIgnoreDirectory(dir) && !dir.equals(rootPath)) {
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.SKIP_SUBTREE;
                                }

                                stats.incrementDirectoriesVisited();
                                return FileVisitResult.CONTINUE;

                            } catch (Exception e) {
                                stats.incrementErrors();
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (stopRequested.getAsBoolean()) {
                                return FileVisitResult.TERMINATE;
                            }

                            try {
                                if (!attrs.isRegularFile()) {
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                if (indexingRules.shouldIgnoreFile(file)) {
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                if (indexingRules.isFileTooLargeForIndexing(attrs.size())) {
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                if (!indexingRules.isSupportedFileType(file)) {
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                discoveredFiles.add(file);
                                stats.incrementFilesDiscovered();

                            } catch (Exception e) {
                                stats.incrementErrors();
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            if (stopRequested.getAsBoolean()) {
                                return FileVisitResult.TERMINATE;
                            }

                            stats.incrementErrors();
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } catch (IOException e) {
            stats.incrementErrors();
        }

        return new CrawlResult(discoveredFiles, stats);
    }
}