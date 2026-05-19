package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RootPathQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return rootPath != null && !rootPath.isBlank();
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        sql.append("AND f.path LIKE ?\n");
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        ps.setString(index++, normalizeRootPrefix(rootPath));
        return index;
    }

    private String normalizeRootPrefix(String rootPath) {
        String normalized = rootPath.replace("\\", "/");

        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized + "%";
    }
}