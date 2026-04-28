package searchengine.db.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:search.db";

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);

        try (Statement stmt = connection.createStatement()) {
            // WAL: rules that allow the 2 connections(READ/WRITE) to not block each other
            stmt.execute("PRAGMA journal_mode=WAL;");
            // NORMAL: faster writes; may lose last changes on crash
            stmt.execute("PRAGMA synchronous=NORMAL;");
        }

        return connection;
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