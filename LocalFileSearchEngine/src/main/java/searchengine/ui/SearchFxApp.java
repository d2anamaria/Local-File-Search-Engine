package searchengine.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import searchengine.config.IndexingRules;
import searchengine.crawler.RecursiveFileCrawler;
import searchengine.db.DatabaseManager;
import searchengine.db.FileIndexRepository;
import searchengine.db.SchemaInitializer;
import searchengine.db.SearchRepository;
import searchengine.extractor.TextExtractor;
import searchengine.indexer.Indexer;
import searchengine.search.SearchService;

import java.sql.Connection;

public class SearchFxApp extends Application {

    private Connection connection;

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager databaseManager = new DatabaseManager();
        connection = databaseManager.getConnection();

        new SchemaInitializer().initializeSchema(connection);

        RecursiveFileCrawler crawler = new RecursiveFileCrawler(new IndexingRules());
        TextExtractor extractor = new TextExtractor();
        FileIndexRepository fileIndexRepository = new FileIndexRepository(connection);
        Indexer indexer = new Indexer(crawler, extractor, fileIndexRepository);

        SearchRepository searchRepository = new SearchRepository(connection);
        SearchService searchService = new SearchService(searchRepository);

        SearchController controller = new SearchController(searchService, indexer, stage);

        Scene scene = new Scene(controller.getView(), 900, 600);

        stage.setTitle("Local File Search");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}