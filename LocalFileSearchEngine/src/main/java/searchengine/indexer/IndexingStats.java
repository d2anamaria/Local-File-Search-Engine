package searchengine.indexer;

public class IndexingStats {
    private int filesIndexed;
    private int filesUnchanged;
    private int filesDeletedFromIndex;
    private int errors;

    public void incrementFilesIndexed() {
        filesIndexed++;
    }

    public void incrementFilesUnchanged() {
        filesUnchanged++;
    }

    public void incrementFilesDeletedFromIndex() {
        filesDeletedFromIndex++;
    }

    public void incrementErrors() {
        errors++;
    }

    public int getFilesIndexed() {
        return filesIndexed;
    }

    public int getFilesUnchanged() {
        return filesUnchanged;
    }

    public int getFilesDeletedFromIndex() {
        return filesDeletedFromIndex;
    }

    public int getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "IndexingStats{" +
                "filesIndexed=" + filesIndexed +
                ", filesUnchanged=" + filesUnchanged +
                ", filesDeletedFromIndex=" + filesDeletedFromIndex +
                ", errors=" + errors +
                '}';
    }
}