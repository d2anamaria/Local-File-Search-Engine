package searchengine.db.repository;

import searchengine.config.IndexingRules;
import searchengine.db.searchquery.*;
import searchengine.search.SearchQuery;
import searchengine.search.SearchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchRepository {

    private final Connection connection;
    private final SearchSqlBuilder sqlBuilder = new SearchSqlBuilder();
    private final SearchParameterBinder parameterBinder = new SearchParameterBinder();
    private final SearchResultMapper resultMapper = new SearchResultMapper();
    private final FileRuleMatcher fileRuleMatcher = new FileRuleMatcher();

    public SearchRepository(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public List<SearchResult> searchByContent(SearchQuery query, IndexingRules rules) {
        return executeSearch(query, null, rules);
    }

    public List<SearchResult> searchByContentUnderRoot(
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        return executeSearch(query, rootPath, rules);
    }

    private List<SearchResult> executeSearch(
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) {
        List<SearchResult> results = new ArrayList<>();

        if (rules.getEnabledTextExtensions() == null || rules.getEnabledTextExtensions().isEmpty()) {
            return results;
        }

        SearchSqlBuilder.SearchSqlContext context = sqlBuilder.build(query, rootPath, rules);

        try (PreparedStatement ps = connection.prepareStatement(context.sql())) {
            parameterBinder.bind(ps, context, query, rootPath, rules);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchResult result = resultMapper.map(rs);

                    if (fileRuleMatcher.matches(result, rules)) {
                        results.add(result);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SEARCH ERROR] " + e.getMessage());
        }

        return results;
    }
}