package searchengine.indexer;

public interface IndexingProgressListener {
    void onCrawlingStarted();
    void onCrawlingFinished(int totalFiles);
    void onIndexingProgress(int current, int total);
}