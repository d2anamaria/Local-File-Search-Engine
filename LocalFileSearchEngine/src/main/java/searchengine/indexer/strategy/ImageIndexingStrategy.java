package searchengine.indexer.strategy;

import searchengine.config.IndexingRules;
import searchengine.extractor.ImageColorExtractor;
import searchengine.indexer.IndexedFileData;

import java.nio.file.Path;

public class ImageIndexingStrategy extends AbstractFileIndexingStrategy {

    private final IndexingRules indexingRules;
    private final ImageColorExtractor colorExtractor=new ImageColorExtractor();

    public ImageIndexingStrategy(IndexingRules indexingRules) {
        this.indexingRules = indexingRules;
    }

    @Override
    public boolean supports(Path file) {
        return indexingRules.isSupportedImageFile(file.getFileName().toString());
    }

    @Override
    public IndexedFileData buildIndexedFileData(
            Path rootPath,
            Path file,
            String modifiedAt
    ) throws Exception {
        String dominantColor = colorExtractor.extractDominantColor(file);

        String preview = dominantColor == null
                ? "image"
                : "dominant color: " + dominantColor;

        return buildFileData(
                rootPath,
                file,
                modifiedAt,
                "image",
                dominantColor,
                preview,
                null
        );
    }
}