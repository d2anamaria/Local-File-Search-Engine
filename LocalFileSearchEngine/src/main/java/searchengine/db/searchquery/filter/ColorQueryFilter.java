package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.db.sql.SearchSql;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ColorQueryFilter implements QueryFilter {

    @Override
    public boolean applies(
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        return query.hasColor();
    }

    @Override
    public void appendSql(
            StringBuilder sql,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        sql.append("AND f.file_category = 'image'\n");

        sql.append("AND LOWER(f.dominant_color) IN (");
        sql.append(SearchSql.placeholders(query.getColor().size()));
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

        for (String color : query.getColor()) {
            ps.setString(index++, color.toLowerCase());
        }

        return index;
    }
}