package searchengine.indexer.strategy;

import searchengine.indexer.IndexedFileData;

import java.nio.file.Path;

public interface FileIndexingStrategy {
    boolean supports(Path file);

    IndexedFileData buildIndexedFileData(
            Path rootPath,
            Path file,
            String modifiedAt // Indexer already computes it for the unchanged-file check
    ) throws Exception;
}