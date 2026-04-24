package searchengine.indexer;

public class IndexedFileData {
    private final String path;
    private final String fileName;
    private final String extension;
    private final String mimeType;
    private final long sizeBytes;
    private final String createdAt;
    private final String modifiedAt;
    private final String indexedAt;
    private final String contentHash;
    private final boolean hidden;
    private final boolean textFile;
    private final String preview;
    private final String content;
    private final int pathDepth;
    private final double directoryScore;
    private final double extensionScore;
    private final double sizeScore;
    private final double pathScore;

    public IndexedFileData(
            String path,
            String fileName,
            String extension,
            String mimeType,
            long sizeBytes,
            String createdAt,
            String modifiedAt,
            String indexedAt,
            String contentHash,
            boolean hidden,
            boolean textFile,
            String preview,
            String content,
            int pathDepth,
            double directoryScore,
            double extensionScore,
            double sizeScore,
            double pathScore
    ) {
        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.indexedAt = indexedAt;
        this.contentHash = contentHash;
        this.hidden = hidden;
        this.textFile = textFile;
        this.preview = preview;
        this.content = content;
        this.pathDepth = pathDepth;
        this.directoryScore = directoryScore;
        this.extensionScore = extensionScore;
        this.sizeScore = sizeScore;
        this.pathScore = pathScore;

    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public String getIndexedAt() {
        return indexedAt;
    }

    public String getContentHash() {
        return contentHash;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isTextFile() {
        return textFile;
    }

    public String getPreview() {
        return preview;
    }

    public String getContent() {
        return content;
    }

    public int getPathDepth() {
        return pathDepth;
    }

    public double getDirectoryScore() {
        return directoryScore;
    }

    public double getExtensionScore() {
        return extensionScore;
    }

    public double getSizeScore() {
        return sizeScore;
    }

    public double getPathScore() {
        return pathScore;
    }
}