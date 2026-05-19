package searchengine.db.searchquery;

import searchengine.config.IndexingRules;
import searchengine.db.searchquery.filter.QueryFilter;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SearchParameterBinder {

    public void bind(
            PreparedStatement ps,
            SearchSqlBuilder.SearchSqlContext context,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        int parameterIndex = 1;

        for (String term : context.terms()) {
            ps.setString(parameterIndex++, term.toLowerCase());
        }

        for (QueryFilter filter : context.activeFilters()) {
            parameterIndex = filter.bind(
                    ps,
                    parameterIndex,
                    query,
                    rootPath,
                    rules
            );
        }
    }
}