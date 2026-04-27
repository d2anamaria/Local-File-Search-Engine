package searchengine.db;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;
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

    public List<SearchResult> searchByContent(SearchQuery query, IndexingRules rules) {
        List<SearchResult> results = new ArrayList<>();
        Set<String> enabledExtensions = rules.getEnabledTextExtensions();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        boolean hasContent = query.hasContent();
        boolean hasPath = query.hasPath();

        String sql = hasContent
                ? SqlQueries.searchContentAndOptionalPathWithRules(
                enabledExtensions.size(),
                !rules.isIncludeHiddenFiles(),
                query.getPath().size()
        )
                : SqlQueries.searchByPathOnlyWithRules(
                enabledExtensions.size(),
                !rules.isIncludeHiddenFiles(),
                query.getPath().size()
        );

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            if (hasContent) {
                ps.setString(parameterIndex++, toFtsAndQuery(query.getContent()));

                if (hasPath) {
                    for (String pathPart : query.getPath()) {
                        ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
                    }
                }
            } else {
                for (String pathPart : query.getPath()) {
                    ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
                }
            }

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
                            rs.getString("preview"),
                            rs.getString("modified_at"),
                            rs.getDouble("path_score")
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
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        List<SearchResult> results = new ArrayList<>();
        Set<String> enabledExtensions = rules.getEnabledTextExtensions();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        boolean hasContent = query.hasContent();
        boolean hasPath = query.hasPath();

        String sql = hasContent
                ? SqlQueries.searchContentAndOptionalPathUnderRootWithRules(
                enabledExtensions.size(),
                !rules.isIncludeHiddenFiles(),
                query.getPath().size()
        )
                : SqlQueries.searchByPathOnlyUnderRootWithRules(
                enabledExtensions.size(),
                !rules.isIncludeHiddenFiles(),
                query.getPath().size()
        );

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            if (hasContent) {
                ps.setString(parameterIndex++, toFtsAndQuery(query.getContent()));
                ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));

                if (hasPath) {
                    for (String pathPart : query.getPath()) {
                        ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
                    }
                }
            } else {
                ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));
                for (String pathPart : query.getPath()) {
                    ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
                }
            }

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
                            rs.getString("preview"),
                            rs.getString("modified_at"),
                            rs.getDouble("path_score")
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

    private String toFtsAndQuery(List<String> contentParts) {
        List<String> terms = new ArrayList<>();

        for (String part : contentParts) {
            for (String word : part.trim().split("\\s+")) {
                if (!word.isBlank()) {
                    terms.add(word + "*");
                }
            }
        }

        return String.join(" AND ", terms);
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