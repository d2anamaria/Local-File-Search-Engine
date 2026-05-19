package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HiddenFileQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return !rules.isIncludeHiddenFiles();
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        sql.append("AND f.is_hidden = ?\n");
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        ps.setInt(index++, 0);
        return index;
    }
}