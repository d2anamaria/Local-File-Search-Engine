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
            String content
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
}