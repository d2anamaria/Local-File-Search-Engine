package searchengine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class TermFileInteractionRepository {

    private static final int MAX_FILES_PER_TERM = 7;

    private final Connection connection;

    public TermFileInteractionRepository(Connection connection) {
        this.connection = connection;
    }

    public void recordTermFileInteraction(String term, String path) {
        if (term == null || term.isBlank() || path == null || path.isBlank()) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.UPSERT_TERM_FILE_INTERACTION)) {
            ps.setString(1, term.toLowerCase());
            ps.setString(2, path);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();

            keepTopFilesForTerm(term.toLowerCase());
        } catch (Exception e) {
            System.out.println("[TERM-FILE INTERACTION ERROR] " + e.getMessage());
        }
    }

    private void keepTopFilesForTerm(String term) {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.KEEP_TOP_FILES_FOR_TERM)) {
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setInt(3, MAX_FILES_PER_TERM);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[TERM-FILE CLEANUP ERROR] " + e.getMessage());
        }
    }
}