package searchengine.db.searchquery;


import searchengine.config.IndexingRules;
import searchengine.search.SearchResult;

import java.nio.file.Path;

public class FileRuleMatcher {

    public boolean matches(SearchResult result, IndexingRules rules) {
        if (result == null) {
            return false;
        }

        String fileName = result.getFileName();
        String pathValue = result.getPath();

        if (rules.isIgnoredFileName(fileName)) {
            return false;
        }

        if (pathValue == null || pathValue.isBlank()) {
            return true;
        }

        try {
            Path path = Path.of(pathValue);

            for (Path part : path) {
                String name = part.toString();

                if (rules.isIgnoredFolder(name)) {
                    return false;
                }
            }
        } catch (Exception ignored) {
        }

        return true;
    }
}