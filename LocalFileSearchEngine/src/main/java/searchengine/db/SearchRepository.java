package searchengine.db;

import searchengine.search.SearchResult;

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

    public List<SearchResult> searchByContent(String query, Set<String> enabledExtensions) {
        List<SearchResult> results = new ArrayList<>();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        String sql = SqlQueries.searchByContentWithExtensions(enabledExtensions.size());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            ps.setString(parameterIndex++, query + "*");

            for (String extension : enabledExtensions) {
                ps.setString(parameterIndex++, extension);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                            rs.getString("file_name"),
                            rs.getString("path"),
                            rs.getString("preview")
                    ));
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
            Set<String> enabledExtensions
    ) {
        List<SearchResult> results = new ArrayList<>();

        if (enabledExtensions == null || enabledExtensions.isEmpty()) {
            return results;
        }

        String sql = SqlQueries.searchByContentUnderRootWithExtensions(enabledExtensions.size());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            ps.setString(parameterIndex++, query + "*");
            ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));

            for (String extension : enabledExtensions) {
                ps.setString(parameterIndex++, extension);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                            rs.getString("file_name"),
                            rs.getString("path"),
                            rs.getString("preview")
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("[SEARCH ERROR] " + e.getMessage());
        }

        return results;
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