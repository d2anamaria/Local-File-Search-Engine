package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PathQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return query.hasPath();
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        for (String ignored : query.getPath()) {
            sql.append("AND LOWER(f.path) LIKE ?\n");
        }
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        for (String pathPart : query.getPath()) {
            ps.setString(index++, "%" + pathPart.toLowerCase() + "%");
        }

        return index;
    }
}