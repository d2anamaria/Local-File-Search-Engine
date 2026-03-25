package searchengine.search;

import searchengine.db.SearchRepository;

import java.util.List;

public class SearchService {

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public List<SearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return searchRepository.searchByContent(query.trim());
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