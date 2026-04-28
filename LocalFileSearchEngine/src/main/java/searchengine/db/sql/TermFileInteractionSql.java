package searchengine.db.sql;

public final class TermFileInteractionSql {

    private TermFileInteractionSql() {}

    public static final String CREATE_TERM_FILE_INTERACTIONS_TABLE = """
    CREATE TABLE IF NOT EXISTS term_file_interactions (
        term TEXT NOT NULL,
        path TEXT NOT NULL,
        score REAL NOT NULL DEFAULT 0,
        last_used TEXT NOT NULL,
        PRIMARY KEY (term, path)
    )
    """;

    public static final String UPSERT_TERM_FILE_INTERACTION = """
    INSERT INTO term_file_interactions(term, path, score, last_used)
    VALUES (?, ?, 1, ?)
    ON CONFLICT(term, path)
    DO UPDATE SET
        score = score * 0.97 + 1,
        last_used = excluded.last_used
    """;

    public static final String KEEP_TOP_FILES_FOR_TERM = """
    DELETE FROM term_file_interactions
    WHERE term = ?
      AND path IN (
          SELECT path
          FROM term_file_interactions
          WHERE term = ?
          ORDER BY score DESC, last_used DESC
          LIMIT -1 OFFSET ?
      )
    """;

    public static final String DELETE_OLD_TERM_FILE_INTERACTIONS = """
    DELETE FROM term_file_interactions
    WHERE julianday('now') - julianday(last_used) > ?
    """;

    public static final String DELETE_ORPHAN_TERM_FILE_INTERACTIONS = """
    DELETE FROM term_file_interactions
    WHERE path NOT IN (
        SELECT path FROM files
    )
    """;
}