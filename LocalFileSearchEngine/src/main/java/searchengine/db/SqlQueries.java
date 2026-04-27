package searchengine.db;

public final class SqlQueries {

    private SqlQueries() {
    }

    public static final String INSERT_OR_REPLACE_FILE = """
    INSERT OR REPLACE INTO files(
        path,
        file_name,
        extension,
        mime_type,
        size_bytes,
        created_at,
        modified_at,
        indexed_at,
        content_hash,
        is_hidden,
        is_text_file,
        path_depth,
        directory_score,
        extension_score,
        size_score,
        path_score,
        preview
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""";

    public static final String DELETE_FTS_BY_PATH = """
        DELETE FROM file_content_fts
        WHERE path = ?
    """;

    public static final String INSERT_FTS_ROW = """
        INSERT INTO file_content_fts(
            file_name,
            path,
            content
        )
        VALUES (?, ?, ?)
    """;

    // help update only modified records
    public static final String FIND_INDEXED_FILES_UNDER_ROOT = """
    SELECT path, modified_at
    FROM files
    WHERE path LIKE ?
""";

    public static final String DELETE_FILE_BY_PATH = """
    DELETE FROM files
    WHERE path = ?
""";

    /// SEARCH HISTORY
    public static final String CREATE_SEARCH_HISTORY_TABLE = """
    CREATE TABLE IF NOT EXISTS search_history (
        query_text TEXT PRIMARY KEY,
        count INTEGER NOT NULL,
        last_used TEXT NOT NULL
    )
""";

    public static final String UPSERT_SEARCH_HISTORY = """
    INSERT INTO search_history(query_text, count, last_used)
    VALUES (?, 1, ?)
    ON CONFLICT(query_text)
    DO UPDATE SET
        count = count + 1,
        last_used = excluded.last_used
""";

    public static final String FIND_SEARCH_SUGGESTIONS = """
    SELECT query_text
    FROM search_history
    WHERE LOWER(query_text) LIKE LOWER(?)
    ORDER BY count DESC, last_used DESC
    LIMIT ?
""";

    public static String searchContentAndOptionalPathWithRules(
            int extensionCount,
            boolean filterHidden,
            boolean filterPath
    ) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = filterPath ? "AND LOWER(f.path) LIKE ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchContentAndOptionalPathUnderRootWithRules(
            int extensionCount,
            boolean filterHidden,
            boolean filterPath
    ) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = filterPath ? "AND LOWER(f.path) LIKE ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          AND f.path LIKE ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyWithRules(int extensionCount, boolean filterHidden) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";

        return """
       SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score
        FROM files f
        WHERE LOWER(f.path) LIKE ?
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyUnderRootWithRules(int extensionCount, boolean filterHidden) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score
        FROM files f
        WHERE f.path LIKE ?
          AND LOWER(f.path) LIKE ?
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(hiddenClause, placeholders(extensionCount));
    }

    private static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}