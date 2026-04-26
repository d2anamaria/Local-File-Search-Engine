package searchengine.search;

public class SearchResult {
    private final String fileName;
    private final String path;
    private final String preview;
    private final String modifiedAt;
    private final double pathScore;

    public SearchResult(String fileName, String path, String preview, String modifiedAt, double pathScore) {
        this.fileName = fileName;
        this.path = path;
        this.preview = preview;
        this.modifiedAt = modifiedAt;
        this.pathScore = pathScore;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getPreview() {
        return preview;
    }

    @Override
    public String toString() {
        return "File: " + fileName + "\n" +
                "Path: " + path + "\n" +
                "Preview:\n" + preview + "\n" +
                "-----";
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public double getPathScore() {
        return pathScore;
    }
}