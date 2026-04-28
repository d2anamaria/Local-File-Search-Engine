package searchengine.db.sql;

public final class FileSql {

    private FileSql() {}

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

    public static final String FIND_INDEXED_FILES_UNDER_ROOT = """
    SELECT path, modified_at
    FROM files
    WHERE path LIKE ?
    """;

    public static final String DELETE_FILE_BY_PATH = """
    DELETE FROM files
    WHERE path = ?
    """;
}