package searchengine.crawler;

import searchengine.config.IndexingRules;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RecursiveFileCrawler {

    private final IndexingRules indexingRules;

    public RecursiveFileCrawler(IndexingRules indexingRules) {
        this.indexingRules = indexingRules;
    }

    public CrawlResult crawl(Path rootPath) {
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
                            try {
                                if (indexingRules.shouldIgnoreDirectory(dir) && !dir.equals(rootPath)) {
                                    System.out.println("[SKIP DIR] " + dir);
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.SKIP_SUBTREE;
                                }

                                stats.incrementDirectoriesVisited();
                                return FileVisitResult.CONTINUE;

                            } catch (Exception e) {
                                System.err.println("[ERROR DIR] " + dir + " -> " + e.getMessage());
                                stats.incrementErrors();
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            try {
                                if (!attrs.isRegularFile()) {
                                    System.out.println("[SKIP NON-REGULAR] " + file);
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                if (indexingRules.shouldIgnoreFile(file)) {
                                    System.out.println("[SKIP FILE] " + file);
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                if (!indexingRules.isSupportedFileType(file)) {
                                    System.out.println("[SKIP UNSUPPORTED] " + file);
                                    stats.incrementFilesSkipped();
                                    return FileVisitResult.CONTINUE;
                                }

                                discoveredFiles.add(file);
                                stats.incrementFilesDiscovered();
                                System.out.println("[FOUND] " + file);

                            } catch (Exception e) {
                                System.err.println("[ERROR FILE] " + file + " -> " + e.getMessage());
                                stats.incrementErrors();
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            System.err.println("[FAILED ACCESS] " + file + " -> " +
                                    (exc != null ? exc.getMessage() : "unknown error"));
                            stats.incrementErrors();
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } catch (IOException e) {
            System.err.println("[CRAWL ERROR] " + e.getMessage());
            stats.incrementErrors();
        }

        return new CrawlResult(discoveredFiles, stats);
    }
}