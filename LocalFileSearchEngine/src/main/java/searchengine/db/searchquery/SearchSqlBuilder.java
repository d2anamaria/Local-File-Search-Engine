package searchengine.db.searchquery;

import searchengine.config.IndexingRules;
import searchengine.db.sql.SearchSql;
import searchengine.search.SearchQuery;
import searchengine.ui.util.QueryTermExtractor;

import java.util.List;

public class SearchSqlBuilder {

    private final QueryTermExtractor queryTermExtractor = new QueryTermExtractor();

    public SearchSqlContext build(SearchQuery query, String rootPath, IndexingRules rules) {
        List<String> terms = queryTermExtractor.extractTerms(query);

        boolean hasContent = query.hasContent();
        boolean underRoot = rootPath != null && !rootPath.isBlank();

        int extensionCount = rules.getEnabledTextExtensions().size();
        boolean filterHidden = !rules.isIncludeHiddenFiles();
        int pathPartCount = query.getPath().size();
        int termCount = terms.size();

        String sql;

        if (underRoot) {
            sql = hasContent
                    ? SearchSql.searchContentAndOptionalPathUnderRootWithRules(
                    extensionCount,
                    filterHidden,
                    pathPartCount,
                    termCount
            )
                    : SearchSql.searchByPathOnlyUnderRootWithRules(
                    extensionCount,
                    filterHidden,
                    pathPartCount,
                    termCount
            );
        } else {
            sql = hasContent
                    ? SearchSql.searchContentAndOptionalPathWithRules(
                    extensionCount,
                    filterHidden,
                    pathPartCount,
                    termCount
            )
                    : SearchSql.searchByPathOnlyWithRules(
                    extensionCount,
                    filterHidden,
                    pathPartCount,
                    termCount
            );
        }

        return new SearchSqlContext(sql, terms, hasContent, underRoot);
    }

    public record SearchSqlContext(
            String sql,
            List<String> terms,
            boolean hasContent,
            boolean underRoot
    ) {
    }
}