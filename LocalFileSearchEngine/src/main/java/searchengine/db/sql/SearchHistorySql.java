package searchengine.db.sql;

public final class SearchHistorySql {

    private SearchHistorySql() {}

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
}