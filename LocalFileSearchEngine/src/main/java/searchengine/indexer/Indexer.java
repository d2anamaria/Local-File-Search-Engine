package searchengine.indexer;

import searchengine.crawler.CrawlResult;
import searchengine.crawler.CrawlStats;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.FileIndexRepository;
import searchengine.extractor.TextExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class Indexer {

    private final RecursiveFileCrawler crawler;
    private final TextExtractor extractor;
    private final FileIndexRepository repository;

    public Indexer(
            RecursiveFileCrawler crawler,
            TextExtractor extractor,
            FileIndexRepository repository
    ) {
        this.crawler = crawler;
        this.extractor = extractor;
        this.repository = repository;
    }

    public CrawlStats index(Path rootPath) {
        CrawlResult crawlResult = crawler.crawl(rootPath);

        for (Path file : crawlResult.getDiscoveredFiles()) {
            try {
                IndexedFileData fileData = buildIndexedFileData(file);
                repository.save(fileData);
                System.out.println("[INDEXED] " + file);
            } catch (Exception e) {
                System.out.println("[ERROR] " + file + " -> " + e.getMessage());
            }
        }

        return crawlResult.getStats();
    }

    private IndexedFileData buildIndexedFileData(Path file) throws Exception {
        String absolutePath = file.toAbsolutePath().toString();
        String fileName = file.getFileName().toString();
        String content = extractor.extractText(file);
        String preview = extractor.preview(content);

        String extension = getExtension(fileName);
        String mimeType = Files.probeContentType(file);
        long sizeBytes = Files.size(file);
        String modifiedAt = Files.getLastModifiedTime(file).toInstant().toString();
        String indexedAt = Instant.now().toString();
        boolean isHidden = Files.isHidden(file);
        boolean isTextFile = content != null && !content.isBlank();

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
                isTextFile,
                preview,
                content
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