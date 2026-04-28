package searchengine.ranking;

import java.util.List;

public final class RankingStrategyRegistry {

    private RankingStrategyRegistry() {
    }

    public static List<RankingStrategy> getAvailableStrategies() {
        return List.of(
                new PathScoreStrategy(),
                new UserRelevanceStrategy(),
                new ModifiedDateStrategy(),
                new AlphabeticalStrategy()
        );
    }
}