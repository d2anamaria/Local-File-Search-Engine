package searchengine;


import searchengine.db.SchemaInitializer;
import searchengine.db.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        SchemaInitializer schemaInitializer = new SchemaInitializer();

        databaseManager.testConnection();

        try (Connection connection = databaseManager.getConnection()) {
            schemaInitializer.initializeSchema(connection);
            System.out.println("Local File Search Engine starting...");
        } catch (SQLException e) {
            System.err.println("Error while initializing database schema.");
            e.printStackTrace();
        }
    }
}
