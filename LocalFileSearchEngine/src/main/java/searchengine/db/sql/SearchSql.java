package searchengine.db.sql;

public final class SearchSql {

    private SearchSql() {}

    private static final String USER_RELEVANCE_SELECT = """
    LOG(1 + COALESCE(ri.click_count, 0)) * 1.0
    + LOG(1 + COALESCE(ri.copy_path_count, 0)) * 2.5
    + LOG(1 + COALESCE(ext.dynamic_extension_count, 0)) * 0.1
    + (1.0 / (1 + (julianday('now') - julianday(f.modified_at)))) * 0.5
    + LOG(1 + COALESCE(tfi.term_file_score, 0)) * 4.0
    AS user_relevance_score
    """;

    public static String searchContentAndOptionalPathWithRules(
            int extensionCount,
            boolean filterHidden,
            int pathCount,
            int termCount
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
        """.formatted(USER_RELEVANCE_SELECT, resultInteractionsJoin(termCount), pathClause, hiddenClause, placeholders(extensionCount));
    }

    public static String searchByPathOnlyWithRules(int extensionCount, boolean filterHidden, int pathCount, int termCount) {
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
        """.formatted(USER_RELEVANCE_SELECT, resultInteractionsJoin(termCount), pathClause, hiddenClause, placeholders(extensionCount));
    }

    private static String resultInteractionsJoin(int termCount) {
        String termFileJoin = termCount <= 0
                ? "LEFT JOIN (SELECT NULL AS path, 0 AS term_file_score) tfi ON tfi.path = f.path"
                : """
                LEFT JOIN (
                    SELECT path, SUM(score) AS term_file_score
                    FROM term_file_interactions
                    WHERE term IN (%s)
                    GROUP BY path
                ) tfi ON tfi.path = f.path
                """.formatted(placeholders(termCount));

        return """
        LEFT JOIN result_interactions ri ON ri.path = f.path
        LEFT JOIN (
            SELECT f2.extension,
                   SUM(ri2.click_count + ri2.copy_path_count) AS dynamic_extension_count
            FROM result_interactions ri2
            JOIN files f2 ON f2.path = ri2.path
            WHERE f2.extension IS NOT NULL AND f2.extension <> ''
            GROUP BY f2.extension
        ) ext ON ext.extension = f.extension
        %s
        """.formatted(termFileJoin);
    }

    public static String searchContentAndOptionalPathUnderRootWithRules(
            int extensionCount,
            boolean filterHidden,
            int pathCount,
            int termCount
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
    """.formatted(
                USER_RELEVANCE_SELECT,
                resultInteractionsJoin(termCount),
                pathClause,
                hiddenClause,
                placeholders(extensionCount)
        );
    }

    public static String searchByPathOnlyUnderRootWithRules(
            int extensionCount,
            boolean filterHidden,
            int pathCount,
            int termCount
    ) {
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
    """.formatted(
                USER_RELEVANCE_SELECT,
                resultInteractionsJoin(termCount),
                pathClause,
                hiddenClause,
                placeholders(extensionCount)
        );
    }

    private static String pathClauses(int count) {
        return "AND LOWER(f.path) LIKE ?\n".repeat(count);
    }

    private static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}