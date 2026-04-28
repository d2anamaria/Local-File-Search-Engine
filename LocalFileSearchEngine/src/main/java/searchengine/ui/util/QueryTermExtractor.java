package searchengine.ui.util;
import searchengine.search.*;
import java.util.ArrayList;
import java.util.List;

public class QueryTermExtractor {

    private static final int MAX_TERMS = 5;

    public List<String> extractTerms(SearchQuery parsedQuery) {
        List<String> rawParts = new ArrayList<>();
        rawParts.addAll(parsedQuery.getContent());
        rawParts.addAll(parsedQuery.getPath());

        return rawParts.stream()
                .flatMap(part -> List.of(part.toLowerCase().split("[\\s/\\\\._-]+")).stream())
                .map(term -> term.replaceAll("[^a-zA-Z0-9_]", ""))
                .filter(term -> term.length() >= 3)
                .distinct()
                .limit(MAX_TERMS)
                .toList();
    }
}
