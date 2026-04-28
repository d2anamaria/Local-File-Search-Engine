package searchengine.search;

public class SearchResult {
    private final String fileName;
    private final String path;
    private final String preview;
    private final String modifiedAt;
    private final double pathScore;
    private final double userRelevanceScore;

    public SearchResult(String fileName, String path, String preview, String modifiedAt, double pathScore,double userRelevanceScore) {
        this.fileName = fileName;
        this.path = path;
        this.preview = preview;
        this.modifiedAt = modifiedAt;
        this.pathScore = pathScore;
        this.userRelevanceScore = userRelevanceScore;
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

    public double getUserRelevanceScore() {
        return userRelevanceScore;
    }
}