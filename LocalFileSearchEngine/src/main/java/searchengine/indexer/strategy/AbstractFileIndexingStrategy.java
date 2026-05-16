package searchengine.indexer.strategy;

import searchengine.indexer.IndexedFileData;
import searchengine.ranking.PathScore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public abstract class AbstractFileIndexingStrategy implements FileIndexingStrategy {

    protected IndexedFileData buildFileData(
            Path rootPath,
            Path file,
            String modifiedAt,
            String fileCategory,
            String dominantColor,
            String preview,
            String content
    ) throws Exception {
        String absolutePath = file.toAbsolutePath().toString();
        String fileName = file.getFileName().toString();
        String extension = getExtension(fileName);
        String mimeType = Files.probeContentType(file);
        long sizeBytes = Files.size(file);
        String indexedAt = Instant.now().toString();
        boolean isHidden = Files.isHidden(file);

        PathScore pathScore = new PathScore(rootPath, file, extension, sizeBytes);

        return new IndexedFileData(
                absolutePath,
                fileName,
                extension,
                mimeType,
                sizeBytes,
                null,
                modifiedAt,
                indexedAt,
                null,
                isHidden,
                fileCategory,
                dominantColor,
                preview,
                content,
                pathScore.getPathDepth(),
                pathScore.getDirectoryScore(),
                pathScore.getExtensionScore(),
                pathScore.getSizeScore(),
                pathScore.getPathScore()
        );
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDot + 1).toLowerCase();
    }
}