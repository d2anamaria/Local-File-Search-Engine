package searchengine;

import searchengine.config.IndexingRules;
import searchengine.crawler.CrawlStats;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.DatabaseManager;
import searchengine.db.FileIndexRepository;
import searchengine.db.SchemaInitializer;
import searchengine.db.SearchRepository;
import searchengine.extractor.TextExtractor;
import searchengine.indexer.Indexer;
import searchengine.search.SearchService;

import java.nio.file.Path;
import java.sql.Connection;

public class App {

    public static void main(String[] args) {
        Path root = Path.of(
                System.getProperty("user.home"),
                "Projects",
                "UTCN",
                "Software Design"
        );

        DatabaseManager dbManager = new DatabaseManager();

        try (Connection connection = dbManager.getConnection()) {
            new SchemaInitializer().initializeSchema(connection);

            RecursiveFileCrawler crawler = new RecursiveFileCrawler(new IndexingRules());
            TextExtractor extractor = new TextExtractor();
            FileIndexRepository fileIndexRepository = new FileIndexRepository(connection);
            SearchRepository searchRepository = new SearchRepository(connection);

            Indexer indexer = new Indexer(crawler, extractor, fileIndexRepository);
            SearchService searchService = new SearchService(searchRepository);

            CrawlStats stats = indexer.index(root);

            System.out.println("Indexing finished.");
            System.out.println(stats);

            System.out.println("\nSearch results for: hello");
            searchService.printResults("hello");

            System.out.println("\nSearch results for: root path");
            searchService.printResults("\"root path\"");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}