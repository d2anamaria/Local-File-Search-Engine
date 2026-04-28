package searchengine.ranking;

import org.junit.jupiter.api.Test;
import searchengine.search.SearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingStrategyTest {

    private SearchResult result(
            String fileName,
            String path,
            String modifiedAt,
            double pathScore,
            double userScore
    ) {
        return new SearchResult(
                fileName,
                path,
                "preview",
                modifiedAt,
                pathScore,
                userScore
        );
    }

    @Test
    void alphabeticalStrategySortsByFileName() {
        List<SearchResult> results = List.of(
                result("zebra.txt", "/tmp/zebra.txt", "2026-01-01T10:00:00", 0, 0),
                result("apple.txt", "/tmp/apple.txt", "2026-01-01T10:00:00", 0, 0)
        );

        List<SearchResult> ranked = new AlphabeticalStrategy().rank(results);

        assertEquals("apple.txt", ranked.get(0).getFileName());
        assertEquals("zebra.txt", ranked.get(1).getFileName());
    }

    @Test
    void pathScoreStrategySortsHighestScoreFirst() {
        List<SearchResult> results = List.of(
                result("low.txt", "/tmp/low.txt", "2026-01-01T10:00:00", 1.0, 0),
                result("high.txt", "/tmp/high.txt", "2026-01-01T10:00:00", 9.0, 0)
        );

        List<SearchResult> ranked = new PathScoreStrategy().rank(results);

        assertEquals("high.txt", ranked.get(0).getFileName());
        assertEquals("low.txt", ranked.get(1).getFileName());
    }

    @Test
    void modifiedDateStrategySortsNewestFirst() {
        List<SearchResult> results = List.of(
                result("old.txt", "/tmp/old.txt", "2025-01-01T10:00:00", 0, 0),
                result("new.txt", "/tmp/new.txt", "2026-01-01T10:00:00", 0, 0)
        );

        List<SearchResult> ranked = new ModifiedDateStrategy().rank(results);

        assertEquals("new.txt", ranked.get(0).getFileName());
        assertEquals("old.txt", ranked.get(1).getFileName());
    }

    @Test
    void userRelevanceStrategySortsHighestUserScoreFirst() {
        List<SearchResult> results = List.of(
                result("low.txt", "/tmp/low.txt", "2026-01-01T10:00:00", 0, 1.0),
                result("high.txt", "/tmp/high.txt", "2026-01-01T10:00:00", 0, 8.0)
        );

        List<SearchResult> ranked = new UserRelevanceStrategy().rank(results);

        assertEquals("high.txt", ranked.get(0).getFileName());
        assertEquals("low.txt", ranked.get(1).getFileName());
    }
}