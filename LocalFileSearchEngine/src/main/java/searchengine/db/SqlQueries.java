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
        count = count * 0.97 + 1,
        last_used = excluded.last_used
""";

    public static final String FIND_SEARCH_SUGGESTIONS = """
    SELECT query_text
    FROM search_history
    WHERE LOWER(query_text) LIKE LOWER(?)
    ORDER BY count DESC, last_used DESC
    LIMIT ?
""";

    public static final String DELETE_OLD_SEARCH_HISTORY = """
    DELETE FROM search_history
    WHERE julianday('now') - julianday(last_used) > ?
    """;

    // USER INTERACTIONS
    public static final String CREATE_RESULT_INTERACTIONS_TABLE = """
    CREATE TABLE IF NOT EXISTS result_interactions (
        path TEXT PRIMARY KEY,
        click_count INTEGER NOT NULL DEFAULT 0,
        copy_path_count INTEGER NOT NULL DEFAULT 0,
        last_interacted_at TEXT NOT NULL
    )
    """;

    public static final String UPSERT_RESULT_CLICK = """
    INSERT INTO result_interactions(path, click_count, copy_path_count, last_interacted_at)
    VALUES (?, 1, 0, ?)
    ON CONFLICT(path)
    DO UPDATE SET
        click_count = click_count * 0.95 + 1,
        last_interacted_at = excluded.last_interacted_at
    """;

    public static final String UPSERT_COPY_PATH = """
    INSERT INTO result_interactions(path, click_count, copy_path_count, last_interacted_at)
    VALUES (?, 0, 1, ?)
    ON CONFLICT(path)
    DO UPDATE SET
        copy_path_count = copy_path_count + 1,
        last_interacted_at = excluded.last_interacted_at
    """;

    public static final String DELETE_OLD_RESULT_INTERACTIONS = """
    DELETE FROM result_interactions
    WHERE julianday('now') - julianday(last_interacted_at) > ?
    """;

    public static final String DELETE_ORPHAN_RESULT_INTERACTIONS = """
    DELETE FROM result_interactions
    WHERE path NOT IN (
        SELECT path FROM files
    )
    """;

    private static final String USER_RELEVANCE_SELECT = """
    LOG(1 + COALESCE(ri.click_count, 0)) * 1.0
    + LOG(1 + COALESCE(ri.copy_path_count, 0)) * 2.5
    + LOG(1 + COALESCE(ext.dynamic_extension_count, 0)) * 0.1
    + (1.0 / (1 + (julianday('now') - julianday(f.modified_at)))) * 0.5
    AS user_relevance_score
""";

    private static final String RESULT_INTERACTIONS_JOIN = """
    LEFT JOIN result_interactions ri ON ri.path = f.path
    LEFT JOIN (
        SELECT
            f2.extension,
            SUM(ri2.click_count + ri2.copy_path_count) AS dynamic_extension_count
        FROM result_interactions ri2
        JOIN files f2 ON f2.path = ri2.path
        WHERE f2.extension IS NOT NULL
          AND f2.extension <> ''
        GROUP BY f2.extension
    ) ext ON ext.extension = f.extension
""";

    public static String searchContentAndOptionalPathWithRules(
            int extensionCount,
            boolean filterHidden,
            int pathCount
    ) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = pathClauses(pathCount);

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score, %s
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        %s
        WHERE file_content_fts MATCH ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(USER_RELEVANCE_SELECT, RESULT_INTERACTIONS_JOIN, pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchContentAndOptionalPathUnderRootWithRules(
            int extensionCount,
            boolean filterHidden,
            int pathCount
    ) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = pathClauses(pathCount);

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score, %s
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        %s
        WHERE file_content_fts MATCH ?
          AND f.path LIKE ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted( USER_RELEVANCE_SELECT, RESULT_INTERACTIONS_JOIN, pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyWithRules(int extensionCount, boolean filterHidden, int pathCount) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = pathClauses(pathCount);

        return """
       SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score, %s
        FROM files f
        %s
        WHERE 1 = 1
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(USER_RELEVANCE_SELECT, RESULT_INTERACTIONS_JOIN, pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyUnderRootWithRules(int extensionCount, boolean filterHidden, int pathCount) {
        String hiddenClause = filterHidden ? "AND f.is_hidden = ?\n" : "";
        String pathClause = pathClauses(pathCount);

        return """
        SELECT f.file_name, f.path, f.preview, f.modified_at, f.path_score, %s
        FROM files f
        %s
        WHERE f.path LIKE ?
          %s
          %s
          AND f.size_bytes <= ?
          AND f.extension IN (%s)
          ORDER BY f.path_score DESC
        """.formatted(USER_RELEVANCE_SELECT, RESULT_INTERACTIONS_JOIN, pathClause, hiddenClause, placeholders(extensionCount));
    }

    private static String pathClauses(int count) {
        return "AND LOWER(f.path) LIKE ?\n".repeat(count);
    }

    private static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}