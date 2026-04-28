package searchengine.search;

import searchengine.db.repository.SearchHistoryRepository;

import java.util.List;

public class SearchHistoryService implements SearchObserver {

    private final SearchHistoryRepository repository;

    public SearchHistoryService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onSearchPerformed(String query) {
        recordSearch(query);
    }

    public void recordSearch(String query) {
        if (query == null || query.isBlank()) {
            return;
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < 3) {
            return;
        }

        repository.recordSearch(normalizedQuery);
    }

    public List<String> findSuggestions(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        return repository.findSuggestions(prefix.trim(), 5);
    }
}