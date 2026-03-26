package searchengine.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import searchengine.db.DatabaseManager;
import searchengine.db.SchemaInitializer;
import searchengine.db.SearchRepository;
import searchengine.search.SearchService;

import java.sql.Connection;

public class SearchFxApp extends Application {

    private Connection connection;

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager databaseManager = new DatabaseManager();
        connection = databaseManager.getConnection();

        new SchemaInitializer().initializeSchema(connection);

        SearchRepository searchRepository = new SearchRepository(connection);
        SearchService searchService = new SearchService(searchRepository);

        SearchController controller = new SearchController(searchService);

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