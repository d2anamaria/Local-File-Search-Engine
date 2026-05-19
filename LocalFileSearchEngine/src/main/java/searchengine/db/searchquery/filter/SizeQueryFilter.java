package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SizeQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return true;
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        sql.append("AND f.size_bytes <= ?\n");
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        ps.setLong(index++, rules.getMaxIndexedFileSizeBytes());
        return index;
    }
}