package searchengine.search;

import searchengine.config.IndexingRules;
import searchengine.db.ResultInteractionRepository;
import searchengine.db.SearchRepository;
import searchengine.ranking.*;
import java.util.ArrayList;
import searchengine.search.ResultInteractionService;
import java.util.List;

public class SearchService {

    private final SearchRepository searchRepository;
    private final IndexingRules indexingRules;
    private final QueryParser queryParser;
    private final List<SearchObserver> searchObservers = new ArrayList<>();
    private final ResultInteractionService resultInteractionService;

    public SearchService(SearchRepository searchRepository, IndexingRules indexingRules) {
        this.searchRepository = searchRepository;
        this.indexingRules = indexingRules;
        this.queryParser = new QueryParser();
        this.resultInteractionService = new ResultInteractionService(
                new ResultInteractionRepository(searchRepository.getConnection())
        );
    }

    public void addSearchObserver(SearchObserver observer) {
        if (observer != null) {
            searchObservers.add(observer);
        }
    }

    private void notifySearchPerformed(String query) {
        for (SearchObserver observer : searchObservers) {
            observer.onSearchPerformed(query);
        }
    }

    public List<String> findSuggestions(String prefix) {
        for (SearchObserver observer : searchObservers) {
            if (observer instanceof SearchHistoryService historyService) {
                return historyService.findSuggestions(prefix);
            }
        }

        return List.of();
    }

    public List<SearchResult> search(String query) {
        SearchQuery parsedQuery = queryParser.parse(query);

        if (parsedQuery.isEmpty()) {
            return List.of();
        }

        return searchRepository.searchByContent(parsedQuery, indexingRules);
    }

    public List<SearchResult> search(String query, String rootPath) {
        return search(query, rootPath, new PathScoreStrategy());
    }

    public List<SearchResult> search(String query, String rootPath, RankingStrategy strategy) {
        SearchQuery parsedQuery = queryParser.parse(query);

        if (parsedQuery.isEmpty()) {
            return List.of();
        }


        List<SearchResult> results;

        if (rootPath == null || rootPath.isBlank()) {
            results = searchRepository.searchByContent(parsedQuery, indexingRules);
        } else {
            results = searchRepository.searchByContentUnderRoot(parsedQuery, rootPath, indexingRules);
        }

        return strategy.rank(results);
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

    public void recordSearchHistory(String query) {
        SearchQuery parsedQuery = queryParser.parse(query);

        if (parsedQuery.isEmpty()) {
            return;
        }

        notifySearchPerformed(query);
    }

    public void recordResultClick(SearchResult result) {
        resultInteractionService.recordClick(result);
    }

    public void recordCopyPath(SearchResult result) {
        resultInteractionService.recordCopyPath(result);
    }
}