package searchengine.search;

import searchengine.config.IndexingRules;
import searchengine.db.SearchRepository;

import java.util.List;

public class SearchService {

    private final SearchRepository searchRepository;
    private final IndexingRules indexingRules;
    private final QueryParser queryParser;

    public SearchService(SearchRepository searchRepository, IndexingRules indexingRules) {
        this.searchRepository = searchRepository;
        this.indexingRules = indexingRules;
        this.queryParser = new QueryParser();
    }

    public List<SearchResult> search(String query) {
        SearchQuery parsedQuery = queryParser.parse(query);

        if (parsedQuery.isEmpty()) {
            return List.of();
        }

        return searchRepository.searchByContent(parsedQuery, indexingRules);
    }

    public List<SearchResult> search(String query, String rootPath) {
        SearchQuery parsedQuery = queryParser.parse(query);

        if (parsedQuery.isEmpty()) {
            return List.of();
        }

        if (rootPath == null || rootPath.isBlank()) {
            return searchRepository.searchByContent(parsedQuery, indexingRules);
        }

        return searchRepository.searchByContentUnderRoot(parsedQuery, rootPath, indexingRules);
    }

    public void printResults(String query) {
        List<SearchResult> results = search(query);

        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        for (SearchResult result : results) {
            System.out.println(result);
        }
    }
}