package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryFilter {

    boolean applies(SearchQuery query, String rootPath, IndexingRules rules);

    void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules);

    int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException;
}