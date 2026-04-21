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


    public static String searchContentAndOptionalPathWithRules(
            int extensionCount,
            boolean filterHidden,
            boolean filterPath
    ) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = filterPath ? "AND LOWER(f.path) LIKE ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
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
        SELECT f.file_name, f.path, f.preview
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        WHERE file_content_fts MATCH ?
          AND f.path LIKE ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
        """.formatted(pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyWithRules(int extensionCount, boolean filterHidden) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview
        FROM files f
        WHERE LOWER(f.path) LIKE ?
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
        """.formatted(hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyUnderRootWithRules(int extensionCount, boolean filterHidden) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";

        return """
        SELECT f.file_name, f.path, f.preview
        FROM files f
        WHERE f.path LIKE ?
          AND LOWER(f.path) LIKE ?
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
        """.formatted(hiddenClause, placeholders(extensionCount));
    }

    private static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}