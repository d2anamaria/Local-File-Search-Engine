package searchengine.ranking;

import searchengine.search.SearchResult;

import java.util.Comparator;
import java.util.List;

public class UserRelevanceStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        return results.stream()
                .sorted(
                        Comparator.comparingDouble(SearchResult::getUserRelevanceScore)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparingDouble(SearchResult::getPathScore)
                                                .reversed()
                                )
                                .thenComparing(SearchResult::getFileName)
                )
                .toList();
    }

    @Override
    public String getDisplayName() {
        return "User relevance";
    }
}