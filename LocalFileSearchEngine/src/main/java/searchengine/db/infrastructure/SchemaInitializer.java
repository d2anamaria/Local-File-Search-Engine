package searchengine.db.infrastructure;

import searchengine.db.sql.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public void initializeSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS files (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    path TEXT NOT NULL UNIQUE,
                    file_name TEXT NOT NULL,
                    extension TEXT,
                    mime_type TEXT,
                    size_bytes INTEGER NOT NULL,
                    created_at TEXT,
                    modified_at TEXT NOT NULL,
                    indexed_at TEXT NOT NULL,
                    content_hash TEXT,
                    is_hidden INTEGER NOT NULL DEFAULT 0,
                    is_text_file INTEGER NOT NULL DEFAULT 0,
                    path_depth INTEGER NOT NULL DEFAULT 0,
                    directory_score REAL NOT NULL DEFAULT 0,
                    extension_score REAL NOT NULL DEFAULT 0,
                    size_score REAL NOT NULL DEFAULT 0,
                    path_score REAL NOT NULL DEFAULT 0,
                    preview TEXT
                );
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_files_file_name
                ON files(file_name);
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_files_extension
                ON files(extension);
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_files_modified_at
                ON files(modified_at);
            """);

            stmt.execute("""
                CREATE VIRTUAL TABLE IF NOT EXISTS file_content_fts
                USING fts5(
                    file_name,
                    path,
                    content
                );
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_files_path_score
                ON files(path_score);
            """);

            stmt.execute(SearchHistorySql.CREATE_SEARCH_HISTORY_TABLE);
            stmt.execute(ResultInteractionSql.CREATE_RESULT_INTERACTIONS_TABLE);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_result_interactions_path
                ON result_interactions(path);
            """);

            stmt.execute(TermFileInteractionSql.CREATE_TERM_FILE_INTERACTIONS_TABLE);
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_term_file_term_score
                ON term_file_interactions(term, score DESC, last_used DESC);
            """);

            System.out.println("Database schema initialized successfully.");
        }
    }
}
