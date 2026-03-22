package searchengine.crawler;

import java.nio.file.Path;
import java.util.List;

public class CrawlResult {
    private final List<Path> discoveredFiles;
    private final CrawlStats stats;

    public CrawlResult(List<Path> discoveredFiles, CrawlStats stats) {
        this.discoveredFiles = discoveredFiles;
        this.stats = stats;
    }

    public List<Path> getDiscoveredFiles() {
        return discoveredFiles;
    }

    public CrawlStats getStats() {
        return stats;
    }
}