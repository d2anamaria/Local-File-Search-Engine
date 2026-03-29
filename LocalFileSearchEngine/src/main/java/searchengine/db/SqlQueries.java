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
            preview
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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

    public static final String SEARCH_BY_CONTENT = """
        SELECT f.file_name, f.path, f.preview
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
    """;

    public static final String SEARCH_BY_CONTENT_UNDER_ROOT = """
    SELECT f.file_name, f.path, f.preview
    FROM file_content_fts fts
    JOIN files f ON f.path = fts.path
    WHERE file_content_fts MATCH ?
      AND f.path LIKE ?
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

    public static String searchByContentWithExtensions(int extensionCount) {
        return """
        SELECT f.file_name, f.path, f.preview
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          AND f.extension IN (%s)
        """.formatted(placeholders(extensionCount));
    }

    public static String searchByContentUnderRootWithExtensions(int extensionCount) {
        return """
        SELECT f.file_name, f.path, f.preview
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          AND f.path LIKE ?
          AND f.extension IN (%s)
        """.formatted(placeholders(extensionCount));
    }

    private static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}