package searchengine.db;

import searchengine.search.SearchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchRepository {

    private final Connection connection;

    public SearchRepository(Connection connection) {
        this.connection = connection;
    }

    public List<SearchResult> searchByContent(String query) {
        List<SearchResult> results = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.SEARCH_BY_CONTENT)) {
            ps.setString(1, query);

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
}