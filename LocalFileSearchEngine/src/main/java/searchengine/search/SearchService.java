package searchengine.search;

import searchengine.config.IndexingRules;
import searchengine.db.SearchRepository;

import java.util.List;

public class SearchService {

    private final SearchRepository searchRepository;
    private final IndexingRules indexingRules;

    public SearchService(SearchRepository searchRepository, IndexingRules indexingRules) {
        this.searchRepository = searchRepository;
        this.indexingRules = indexingRules;
    }

    public List<SearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return searchRepository.searchByContent(query.trim(), indexingRules);
    }

    public List<SearchResult> search(String query, String rootPath) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        if (rootPath == null || rootPath.isBlank()) {
            return searchRepository.searchByContent(query.trim(), indexingRules);
        }

        return searchRepository.searchByContentUnderRoot(
                query.trim(),
                rootPath,
                indexingRules
        );
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