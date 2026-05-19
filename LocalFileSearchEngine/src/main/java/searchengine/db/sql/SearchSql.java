package searchengine.db.sql;

public final class SearchSql {

    private SearchSql() {}

    public static String baseSelect() {
        return """
        SELECT f.file_name,
               f.path,
               f.preview,
               f.modified_at,
               f.path_score,
               %s
        """.formatted(userRelevanceSelect());
    }

    public static String contentFromClause() {
        return """
        FROM file_content_fts fts
        JOIN files f ON f.path = fts.path
        """;
    }

    public static String filesFromClause() {
        return "FROM files f\n";
    }

    public static String resultInteractionsJoin(int termCount) {
        String termFileJoin = termCount <= 0
                ? "LEFT JOIN (SELECT NULL AS path, 0 AS term_file_score) tfi ON tfi.path = f.path\n"
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
            WHERE f2.extension IS NOT NULL
              AND f2.extension <> ''
            GROUP BY f2.extension
        ) ext ON ext.extension = f.extension
        %s
        """.formatted(termFileJoin);
    }

    public static String whereStart() {
        return "WHERE 1 = 1\n";
    }

    public static String defaultOrderBy() {
        return "ORDER BY f.path_score DESC\n";
    }

    public static String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }

    private static String userRelevanceSelect() {
        return """
        LOG(1 + COALESCE(ri.click_count, 0)) * 1.0
        + LOG(1 + COALESCE(ri.copy_path_count, 0)) * 2.5
        + LOG(1 + COALESCE(ext.dynamic_extension_count, 0)) * 0.1
        + (1.0 / (1 + (julianday('now') - julianday(f.modified_at)))) * 0.5
        + LOG(1 + COALESCE(tfi.term_file_score, 0)) * 4.0
        AS user_relevance_score
        """;
    }
}