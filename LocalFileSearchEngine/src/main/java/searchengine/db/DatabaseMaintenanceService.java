package searchengine.db;

import java.sql.*;

public class DatabaseMaintenanceService {

    private final DatabaseManager databaseManager;

    public DatabaseMaintenanceService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void runStartupMaintenanceAsync() {
        Thread cleanupThread = new Thread(() -> {
            try (Connection connection = databaseManager.getConnection()) {
                //delete if older than 12 hours (use longer retention in production)
                deleteOldResultInteractions(connection, 0.5);
                deleteOldSearchHistory(connection, 0.5);
                deleteOrphanResultInteractions(connection);
            } catch (Exception e) {
                System.out.println("[DB MAINTENANCE ERROR] " + e.getMessage());
            }
        }, "db-maintenance-thread");

        cleanupThread.setDaemon(true);
        cleanupThread.start();

    }

    private void deleteOldResultInteractions(Connection connection, double days) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.DELETE_OLD_RESULT_INTERACTIONS)) {
            ps.setDouble(1, days);
            ps.executeUpdate();
            deleteOldTermFileInteractions(connection, 90);
            deleteOrphanTermFileInteractions(connection);
        }
    }

    private void deleteOldSearchHistory(Connection connection, double days) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.DELETE_OLD_SEARCH_HISTORY)) {
            ps.setDouble(1, days);
            int deleted = ps.executeUpdate();
            System.out.println("[DB MAINTENANCE] Deleted " + deleted + " old search history entries");
        }
    }

    private void deleteOrphanResultInteractions(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.DELETE_ORPHAN_RESULT_INTERACTIONS)) {
            int deleted = ps.executeUpdate();
            System.out.println("[DB MAINTENANCE] Deleted " + deleted + " orphan interactions");
        }
    }

    private void deleteOldTermFileInteractions(Connection connection, double days) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.DELETE_OLD_TERM_FILE_INTERACTIONS)) {
            ps.setDouble(1, days);
            int deleted = ps.executeUpdate();
            System.out.println("[DB MAINTENANCE] Deleted " + deleted + " old term-file interactions");
        }
    }

    private void deleteOrphanTermFileInteractions(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.DELETE_ORPHAN_TERM_FILE_INTERACTIONS)) {
            int deleted = ps.executeUpdate();
            System.out.println("[DB MAINTENANCE] Deleted " + deleted + " orphan term-file interactions");
        }
    }
}
