package searchengine.search;

public class SearchQuery {
    private final String content;
    private final String path;

    public SearchQuery(String content, String path) {
        this.content = content == null ? "" : content.trim();
        this.path = path == null ? "" : path.trim();
    }

    public String getContent() { return content; }
    public String getPath() { return path; }

    public boolean hasContent() { return !content.isBlank(); }
    public boolean hasPath() { return !path.isBlank(); }

    public boolean isEmpty() {
        return !hasContent() && !hasPath();
    }
}
