package searchengine.indexer.strategy;

import searchengine.config.IndexingRules;

import java.util.List;

public final class IndexingStrategyRegistry {

    private IndexingStrategyRegistry() {}

    public static List<FileIndexingStrategy> getAvailableStrategies(IndexingRules rules) {
        return List.of(
                new TextIndexingStrategy(rules),
                new ImageIndexingStrategy(rules)
        );
    }
}