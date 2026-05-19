package searchengine.db.searchquery.filter;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContentQueryFilter implements QueryFilter {

    @Override
    public boolean applies(SearchQuery query, String rootPath, IndexingRules rules) {
        return query.hasContent();
    }

    @Override
    public void appendSql(StringBuilder sql, SearchQuery query, String rootPath, IndexingRules rules) {
        sql.append("AND file_content_fts MATCH ?\n");
    }

    @Override
    public int bind(
            PreparedStatement ps,
            int index,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        ps.setString(index++, toFtsAndQuery(query.getContent()));
        return index;
    }

    private String toFtsAndQuery(List<String> contentParts) {
        List<String> terms = new ArrayList<>();

        for (String part : contentParts) {
            for (String word : part.trim().split("\\s+")) {
                if (!word.isBlank()) {
                    terms.add(word + "*");
                }
            }
        }

        return String.join(" AND ", terms);
    }
}