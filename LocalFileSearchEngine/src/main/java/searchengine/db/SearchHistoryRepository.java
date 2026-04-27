package searchengine.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryRepository {

    private final Connection connection;

    public SearchHistoryRepository(Connection connection) {
        this.connection = connection;
    }

    public void recordSearch(String queryText) {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.UPSERT_SEARCH_HISTORY)) {
            ps.setString(1, queryText);
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record search history", e);
        }
    }

    public List<String> findSuggestions(String prefix, int limit) {
        List<String> suggestions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.FIND_SEARCH_SUGGESTIONS)) {
            ps.setString(1, prefix + "%");
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(rs.getString("query_text"));
                }
            }

            return suggestions;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load search suggestions", e);
        }
    }
}