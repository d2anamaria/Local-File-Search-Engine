package searchengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:search.db";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Connected to SQLite successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to SQLite.");
            e.printStackTrace();
        }
    }
}