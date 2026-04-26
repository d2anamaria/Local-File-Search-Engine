package searchengine.ranking;

import searchengine.search.SearchResult;

import java.util.Comparator;
import java.util.List;

public class AlphabeticalStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        return results.stream()
                .sorted(Comparator.comparing(SearchResult::getFileName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public String getDisplayName() {
        return "Alphabetical";
    }
}