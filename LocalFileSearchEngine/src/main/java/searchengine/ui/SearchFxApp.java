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
import searchengine.ui.controller.MainController;

import java.sql.Connection;

public class SearchFxApp extends Application {

    private Connection indexingConnection;
    private Connection searchConnection;

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager databaseManager = new DatabaseManager();

        this.indexingConnection = databaseManager.getConnection();
        this.searchConnection = databaseManager.getConnection();

        new SchemaInitializer().initializeSchema(indexingConnection);

        IndexingRules indexingRules = new IndexingRules();

        RecursiveFileCrawler crawler = new RecursiveFileCrawler(indexingRules);
        TextExtractor extractor = new TextExtractor(indexingRules);

        FileIndexRepository fileIndexRepository = new FileIndexRepository(indexingConnection);
        Indexer indexer = new Indexer(crawler, extractor, fileIndexRepository);

        SearchRepository searchRepository = new SearchRepository(searchConnection);
        SearchService searchService = new SearchService(searchRepository, indexingRules);

        MainController controller = new MainController(
                searchService,
                indexer,
                indexingRules,
                stage
        );

        Scene scene = new Scene(controller.getView(), 1000, 650);

        stage.setTitle("Local File Search");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (indexingConnection != null && !indexingConnection.isClosed()) {
            indexingConnection.close();
        }
        if (searchConnection != null && !searchConnection.isClosed()) {
            searchConnection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}