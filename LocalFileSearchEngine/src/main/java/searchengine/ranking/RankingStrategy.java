package searchengine.ranking;

import searchengine.search.SearchResult;

import java.util.List;

public interface RankingStrategy {
    List<SearchResult> rank(List<SearchResult> results);

    String getDisplayName();
}