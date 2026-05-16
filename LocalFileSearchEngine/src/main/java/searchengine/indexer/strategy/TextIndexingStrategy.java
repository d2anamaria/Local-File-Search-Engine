package searchengine.indexer.strategy;

import searchengine.config.IndexingRules;
import searchengine.extractor.TextExtractor;
import searchengine.indexer.IndexedFileData;

import java.nio.file.Path;

public class TextIndexingStrategy extends AbstractFileIndexingStrategy {

    private final IndexingRules indexingRules;
    private final TextExtractor extractor=new TextExtractor();

    public TextIndexingStrategy(IndexingRules indexingRules) {
        this.indexingRules = indexingRules;
    }

    @Override
    public boolean supports(Path file) {
        return indexingRules.isSupportedTextFile(file.getFileName().toString());
    }

    @Override
    public IndexedFileData buildIndexedFileData(
            Path rootPath,
            Path file,
            String modifiedAt
    ) throws Exception {
        String content = extractor.extractText(file);
        String preview = extractor.preview(content);

        return buildFileData(
                rootPath,
                file,
                modifiedAt,
                "text",
                null,
                preview,
                content
        );
    }
}