package searchengine.ranking;

import searchengine.search.SearchResult;

import java.util.Comparator;
import java.util.List;

public class ModifiedDateStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        return results.stream()
                .sorted(Comparator.comparing(SearchResult::getModifiedAt).reversed())
                .toList();
    }

    @Override
    public String getDisplayName() {
        return "Recently modified";
    }
}