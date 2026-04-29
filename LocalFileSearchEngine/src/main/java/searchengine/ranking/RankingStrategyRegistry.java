package searchengine.ranking;

import java.util.List;

public final class RankingStrategyRegistry {

    private RankingStrategyRegistry() {
    }

    public static List<RankingStrategy> getAvailableStrategies() {
        return List.of(
                new UserRelevanceStrategy(),
                new PathScoreStrategy(),
                new ModifiedDateStrategy(),
                new AlphabeticalStrategy()
        );
    }
}