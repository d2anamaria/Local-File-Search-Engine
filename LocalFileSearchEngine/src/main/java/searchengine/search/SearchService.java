package searchengine.search;

import searchengine.config.IndexingRules;
import searchengine.db.SearchRepository;

import java.util.List;
import java.util.Set;

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

        Set<String> enabledExtensions = indexingRules.getEnabledTextExtensions();
        return searchRepository.searchByContent(query.trim(), enabledExtensions);
    }

    public List<SearchResult> search(String query, String rootPath) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Set<String> enabledExtensions = indexingRules.getEnabledTextExtensions();

        if (rootPath == null || rootPath.isBlank()) {
            return searchRepository.searchByContent(query.trim(), enabledExtensions);
        }

        return searchRepository.searchByContentUnderRoot(
                query.trim(),
                rootPath,
                enabledExtensions
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