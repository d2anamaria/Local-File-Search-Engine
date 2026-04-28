package searchengine.db.sql;

public final class ResultInteractionSql {

    private ResultInteractionSql() {}

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
}