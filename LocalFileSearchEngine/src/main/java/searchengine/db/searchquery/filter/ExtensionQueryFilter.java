package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ExtensionQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return !query.hasColor()
                && rules.getEnabledTextExtensions() != null
                && !rules.getEnabledTextExtensions().isEmpty();
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        sql.append("AND f.extension IN (");
        sql.append(placeholders(rules.getEnabledTextExtensions().size()));
        sql.append(")\n");
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        for (String extension : rules.getEnabledTextExtensions()) {
            ps.setString(index++, extension);
        }

        return index;
    }

    private String placeholders(int count) {
        return "?,".repeat(count).replaceAll(",$", "");
    }
}