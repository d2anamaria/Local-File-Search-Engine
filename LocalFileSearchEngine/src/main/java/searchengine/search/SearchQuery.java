package searchengine.search;

import java.util.List;

public class SearchQuery {
    private final List<String> content;
    private final List<String>  path;

    public SearchQuery(List<String>  content, List<String>  path) {
        this.content = content;
        this.path = path;
    }

    public List<String>  getContent() { return content; }
    public List<String>  getPath() { return path; }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public boolean hasPath() {
        return path != null && !path.isEmpty();
    }

    public boolean isEmpty() {
        return !hasContent() && !hasPath();
    }

}
