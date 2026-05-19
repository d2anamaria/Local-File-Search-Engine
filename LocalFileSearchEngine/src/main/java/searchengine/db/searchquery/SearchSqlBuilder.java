package searchengine.db.searchquery;

import searchengine.config.IndexingRules;
import searchengine.db.searchquery.filter.QueryFilter;
import searchengine.db.searchquery.filter.QueryFilterRegistry;
import searchengine.db.sql.SearchSql;
import searchengine.search.SearchQuery;
import searchengine.ui.util.QueryTermExtractor;

import java.util.ArrayList;
import java.util.List;

public class SearchSqlBuilder {

    private final QueryTermExtractor queryTermExtractor =
            new QueryTermExtractor();

    public SearchSqlContext build(
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        List<String> terms =
                queryTermExtractor.extractTerms(query); // used for term_file_interactions, not search

        List<QueryFilter> activeFilters =
                findActiveFilters(query, rootPath, rules);

        StringBuilder sql = new StringBuilder();

        sql.append(SearchSql.baseSelect());

        if (query.hasContent()) {
            sql.append(SearchSql.contentFromClause());
        } else {
            sql.append(SearchSql.filesFromClause());
        }

        sql.append(SearchSql.resultInteractionsJoin(terms.size()));
        sql.append(SearchSql.whereStart());

        for (QueryFilter filter : activeFilters) {
            filter.appendSql(sql, query, rootPath, rules);
        }

        sql.append(SearchSql.defaultOrderBy());

        return new SearchSqlContext(
                sql.toString(),
                terms,
                activeFilters
        );
    }

    private List<QueryFilter> findActiveFilters(
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        List<QueryFilter> activeFilters = new ArrayList<>();

        for (QueryFilter filter : QueryFilterRegistry.filters()) {
            if (filter.applies(query, rootPath, rules)) {
                activeFilters.add(filter);
            }
        }

        return activeFilters;
    }

    public record SearchSqlContext(
            String sql,
            List<String> terms,
            List<QueryFilter> activeFilters
    ) {
    }
}