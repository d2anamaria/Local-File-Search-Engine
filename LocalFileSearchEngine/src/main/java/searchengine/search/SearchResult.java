package searchengine.search;

public class SearchResult {
    private final String fileName;
    private final String path;
    private final String preview;

    public SearchResult(String fileName, String path, String preview) {
        this.fileName = fileName;
        this.path = path;
        this.preview = preview;
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
}