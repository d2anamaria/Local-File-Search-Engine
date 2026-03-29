package searchengine;

import searchengine.config.IndexingRules;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.DatabaseManager;
import searchengine.db.FileIndexRepository;
import searchengine.db.SchemaInitializer;
import searchengine.db.SearchRepository;
import searchengine.extractor.TextExtractor;
import searchengine.indexer.Indexer;
import searchengine.indexer.IndexingResult;
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

            IndexingRules indexingRules = new IndexingRules();
            RecursiveFileCrawler crawler = new RecursiveFileCrawler(indexingRules);
            TextExtractor extractor = new TextExtractor(indexingRules);
            FileIndexRepository fileIndexRepository = new FileIndexRepository(connection);
            SearchRepository searchRepository = new SearchRepository(connection);

            Indexer indexer = new Indexer(crawler, extractor, fileIndexRepository);
            SearchService searchService = new SearchService(searchRepository, indexingRules);

            IndexingResult result = indexer.index(root, null);

            System.out.println("Indexing finished.");
            System.out.println(result.toDisplayText());

            System.out.println("\nSearch results for: hello");
            searchService.printResults("hello");

            System.out.println("\nSearch results for: root path");
            searchService.printResults("\"root path\"");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}