package searchengine.ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;

import java.util.List;

public class SearchController {

    private final SearchService searchService;

    private final BorderPane root = new BorderPane();

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Label statusLabel = new Label("Type a word or phrase to search.");
    private final ListView<SearchResult> resultsList = new ListView<>();

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
        buildUi();
        bindActions();
    }

    public Parent getView() {
        return root;
    }

    private void buildUi() {
        Label titleLabel = new Label("Local File Search");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        searchField.setPromptText("Search content... e.g. hello or \"root path\"");
        searchField.setPrefWidth(500);

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(10, titleLabel, searchBar, statusLabel);
        topBox.setPadding(new Insets(16));

        resultsList.setCellFactory(list -> new SearchResultCell());
        resultsList.setPlaceholder(new Label("No results yet."));

        root.setTop(topBox);
        root.setCenter(resultsList);
        BorderPane.setMargin(resultsList, new Insets(0, 16, 16, 16));
    }

    private void bindActions() {
        searchButton.setOnAction(event -> performSearch());

        searchField.setOnAction(event -> performSearch());

        ChangeListener<String> liveSearchListener = (obs, oldValue, newValue) -> performSearch();
        searchField.textProperty().addListener(liveSearchListener);
    }

    private void performSearch() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            resultsList.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Type a word or phrase to search.");
            return;
        }

        try {
            List<SearchResult> results = searchService.search(query);

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