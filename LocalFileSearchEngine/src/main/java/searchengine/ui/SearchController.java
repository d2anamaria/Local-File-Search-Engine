package searchengine.ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import searchengine.crawler.CrawlStats;
import searchengine.indexer.Indexer;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class SearchController {

    private final SearchService searchService;
    private final Indexer indexer;
    private final Stage stage;

    private String selectedRootPath;

    private final BorderPane root = new BorderPane();

    private final TextField pathField = new TextField();
    private final Button browseButton = new Button("Browse");

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Label statusLabel = new Label("Choose a folder, then search.");
    private final ListView<SearchResult> resultsList = new ListView<>();

    public SearchController(SearchService searchService, Indexer indexer, Stage stage) {
        this.searchService = searchService;
        this.indexer = indexer;
        this.stage = stage;
        buildUi();
        bindActions();
    }

    public Parent getView() {
        return root;
    }

    private void buildUi() {
        Label titleLabel = new Label("Local File Search");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        pathField.setPromptText("Choose root folder to index...");
        pathField.setPrefWidth(500);

        HBox pathBar = new HBox(10, pathField, browseButton);
        pathBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search content... e.g. hello or \"root path\"");
        searchField.setPrefWidth(500);

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(10, titleLabel, pathBar, searchBar, statusLabel);
        topBox.setPadding(new Insets(16));

        resultsList.setCellFactory(list -> new SearchResultCell());
        resultsList.setPlaceholder(new Label("No results yet."));

        root.setTop(topBox);
        root.setCenter(resultsList);
        BorderPane.setMargin(resultsList, new Insets(0, 16, 16, 16));
    }

    private void bindActions() {
        browseButton.setOnAction(event -> chooseDirectory());

        searchButton.setOnAction(event -> performSearch());

        searchField.setOnAction(event -> performSearch());

        ChangeListener<String> liveSearchListener = (obs, oldValue, newValue) -> performSearch();
        searchField.textProperty().addListener(liveSearchListener);
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Index");

        File selectedDirectory = chooser.showDialog(stage);

        if (selectedDirectory != null) {
            selectedRootPath = selectedDirectory.getAbsolutePath();

            pathField.setText(selectedRootPath);
            statusLabel.setText("Selected folder: " + selectedRootPath);

            performIndex();
        }
    }

    private void performIndex() {
        String pathText = pathField.getText();

        if (pathText == null || pathText.isBlank()) {
            statusLabel.setText("Please choose a folder first.");
            return;
        }

        try {
            CrawlStats stats = indexer.index(Path.of(pathText));
            statusLabel.setText("Indexing finished. " + stats);
        } catch (Exception e) {
            statusLabel.setText("Indexing failed: " + e.getMessage());
        }
    }

    private void performSearch() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            resultsList.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<SearchResult> results = searchService.search(query, selectedRootPath);
            resultsList.setItems(FXCollections.observableArrayList(results));

            if (results.isEmpty()) {
                statusLabel.setText("No results found for: " + query);
            } else {
                statusLabel.setText(results.size() + " result(s) for: " + query);
            }
        } catch (Exception e) {
            resultsList.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Search failed: " + e.getMessage());
        }
    }
}