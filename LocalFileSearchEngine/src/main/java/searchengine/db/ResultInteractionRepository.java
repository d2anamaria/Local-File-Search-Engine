package searchengine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class ResultInteractionRepository {

    private final Connection connection;

    public ResultInteractionRepository(Connection connection) {
        this.connection = connection;
    }

    public void recordClick(String path) {
        recordInteraction(SqlQueries.UPSERT_RESULT_CLICK, path);
    }

    public void recordCopyPath(String path) {
        recordInteraction(SqlQueries.UPSERT_COPY_PATH, path);
    }

    private void recordInteraction(String sql, String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[RESULT INTERACTION ERROR] " + e.getMessage());
        }
    }
}