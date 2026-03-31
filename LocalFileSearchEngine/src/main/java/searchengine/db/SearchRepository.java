package searchengine.db;

import searchengine.config.IndexingRules;
import searchengine.search.SearchResult;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchRepository {

    private final Connection connection;

    public SearchRepository(Connection connection) {
        this.connection = connection;
    }

    public List<SearchResult> searchByContent(String query, IndexingRules rules) {
        List<SearchResult> results = new ArrayList<>();
        Set<String> enabledExtensions = rules.getEnabledTextExtensions();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        String sql = SqlQueries.searchByContentWithRules(enabledExtensions.size(), !rules.isIncludeHiddenFiles());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            ps.setString(parameterIndex++, query + "*");

            if (!rules.isIncludeHiddenFiles()) {
                ps.setInt(parameterIndex++, 0);
            }

            ps.setLong(parameterIndex++, rules.getMaxIndexedFileSizeBytes());

            for (String extension : enabledExtensions) {
                ps.setString(parameterIndex++, extension);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchResult result = new SearchResult(
                            rs.getString("file_name"),
                            rs.getString("path"),
                            rs.getString("preview")
                    );

                    if (matchesRuntimeFileFilters(result, rules)) {
                        results.add(result);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SEARCH ERROR] " + e.getMessage());
        }

        return results;
    }

    public List<SearchResult> searchByContentUnderRoot(
            String query,
            String rootPath,
            IndexingRules rules
    ) {
        List<SearchResult> results = new ArrayList<>();
        Set<String> enabledExtensions = rules.getEnabledTextExtensions();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        String sql = SqlQueries.searchByContentUnderRootWithRules(
                enabledExtensions.size(),
                !rules.isIncludeHiddenFiles()
        );

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            ps.setString(parameterIndex++, query + "*");
            ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));

            if (!rules.isIncludeHiddenFiles()) {
                ps.setInt(parameterIndex++, 0);
            }

            ps.setLong(parameterIndex++, rules.getMaxIndexedFileSizeBytes());

            for (String extension : enabledExtensions) {
                ps.setString(parameterIndex++, extension);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchResult result = new SearchResult(
                            rs.getString("file_name"),
                            rs.getString("path"),
                            rs.getString("preview")
                    );

                    if (matchesRuntimeFileFilters(result, rules)) {
                        results.add(result);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SEARCH ERROR] " + e.getMessage());
        }

        return results;
    }

    private boolean matchesRuntimeFileFilters(SearchResult result, IndexingRules rules) {
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

    private String normalizeRootPrefix(String rootPath) {
        if (rootPath == null || rootPath.isBlank()) {
            return "%";
        }

        String normalized = rootPath.replace("\\", "/");

        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized + "%";
    }
}