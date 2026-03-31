package searchengine.indexer;

import searchengine.crawler.CrawlStats;


public class IndexingResult {
    private final CrawlStats crawlStats;
    private final IndexingStats indexingStats;

    private final long totalDurationMillis;
    private final long indexingDurationMillis;
    private final boolean cancelled;

    public IndexingResult(
            CrawlStats crawlStats,
            IndexingStats indexingStats,
            long totalDurationMillis,
            long indexingDurationMillis
    ) {
        this(crawlStats, indexingStats, totalDurationMillis, indexingDurationMillis, false);
    }

    public IndexingResult(
            CrawlStats crawlStats,
            IndexingStats indexingStats,
            long totalDurationMillis,
            long indexingDurationMillis,
            boolean cancelled
    ) {
        this.crawlStats = crawlStats;
        this.indexingStats = indexingStats;
        this.totalDurationMillis = totalDurationMillis;
        this.indexingDurationMillis = indexingDurationMillis;
        this.cancelled = cancelled;
    }

    public CrawlStats getCrawlStats() {
        return crawlStats;
    }

    public IndexingStats getIndexingStats() {
        return indexingStats;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public long getIndexingDurationMillis() {
        return indexingDurationMillis;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String toDisplayText() {
        String statusText = cancelled ? "Stopped by user" : "Completed";

        int discovered = crawlStats.getFilesDiscovered();
        int processed = indexingStats.getFilesIndexed() + indexingStats.getFilesUnchanged();
        int remaining = Math.max(0, discovered - processed);

        String remainingText = cancelled
                ? "\nApprox. remaining files (to index): " + remaining
                : "";

        return """
        Status
        %s

        Crawl
        Directories visited: %d
        Files discovered: %d
        Files skipped: %d
        Crawl errors: %d

        Indexing
        Files indexed: %d
        Unchanged files: %d
        Deleted from index: %d
        Indexing errors: %d%s

        Timing
        Total duration: %s
        Processing duration: %s
        """.formatted(
                statusText,
                crawlStats.getDirectoriesVisited(),
                discovered,
                crawlStats.getFilesSkipped(),
                crawlStats.getErrors(),
                indexingStats.getFilesIndexed(),
                indexingStats.getFilesUnchanged(),
                indexingStats.getFilesDeletedFromIndex(),
                indexingStats.getErrors(),
                remainingText,
                formatDuration(totalDurationMillis),
                formatDuration(indexingDurationMillis)
        );
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long millis = durationMillis % 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return "%dh %dm %ds %dms".formatted(hours, minutes, seconds, millis);
        }

        if (minutes > 0) {
            return "%dm %ds %dms".formatted(minutes, seconds, millis);
        }

        if (seconds > 0) {
            return "%ds %dms".formatted(seconds, millis);
        }

        return "%dms".formatted(millis);
    }
}