package searchengine.ranking;

import searchengine.search.SearchResult;

import java.util.Comparator;
import java.util.List;

public class PathScoreStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        return results.stream()
                .sorted(Comparator.comparingDouble(SearchResult::getPathScore).reversed())
                .toList();
    }

    @Override
    public String getDisplayName() {
        return "Path score";
    }
}