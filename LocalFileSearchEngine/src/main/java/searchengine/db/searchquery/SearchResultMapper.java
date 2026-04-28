package searchengine.db.searchquery;

import searchengine.search.SearchResult;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchResultMapper {

    public SearchResult map(ResultSet rs) throws SQLException {
        return new SearchResult(
                rs.getString("file_name"),
                rs.getString("path"),
                rs.getString("preview"),
                rs.getString("modified_at"),
                rs.getDouble("path_score"),
                rs.getDouble("user_relevance_score")
        );
    }
}