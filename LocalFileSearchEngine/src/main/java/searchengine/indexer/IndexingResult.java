package searchengine.indexer;

import searchengine.crawler.CrawlStats;

public class IndexingResult {
    private final CrawlStats crawlStats;
    private final IndexingStats indexingStats;

    public IndexingResult(CrawlStats crawlStats, IndexingStats indexingStats) {
        this.crawlStats = crawlStats;
        this.indexingStats = indexingStats;
    }

    public CrawlStats getCrawlStats() {
        return crawlStats;
    }

    public IndexingStats getIndexingStats() {
        return indexingStats;
    }

    public String toDisplayText() {
        return """
                Crawl
                Directories visited: %d
                Files discovered: %d
                Files skipped: %d
                Crawl errors: %d

                Indexing
                Files indexed: %d
                Unchanged files: %d
                Deleted from index: %d
                Indexing errors: %d
                """.formatted(
                crawlStats.getDirectoriesVisited(),
                crawlStats.getFilesDiscovered(),
                crawlStats.getFilesSkipped(),
                crawlStats.getErrors(),
                indexingStats.getFilesIndexed(),
                indexingStats.getFilesUnchanged(),
                indexingStats.getFilesDeletedFromIndex(),
                indexingStats.getErrors()
        );
    }
}