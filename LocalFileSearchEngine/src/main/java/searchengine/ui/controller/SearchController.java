package searchengine.ui.controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;
import searchengine.ui.component.SearchResultCell;
import searchengine.ui.component.SearchResultDetailsDialog;
import searchengine.ranking.*;

import java.util.List;
import java.util.function.Supplier;

public class SearchController {

    private final SearchService searchService;
    private final Stage stage;
    private final Supplier<String> rootPathSupplier;

    private final VBox searchBarView = new VBox();
    private final ListView<SearchResult> resultsList = new ListView<>();

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Label statusLabel = new Label("No results yet.");

    private final ComboBox<RankingStrategy> rankingComboBox = new ComboBox<>();

    private final Supplier<Boolean> indexingInProgressSupplier;

    public SearchController(
            SearchService searchService,
            Stage stage,
            Supplier<String> rootPathSupplier,
            Supplier<Boolean> indexingInProgressSupplier
    ) {
        this.searchService = searchService;
        this.stage = stage;
        this.rootPathSupplier = rootPathSupplier;
        this.indexingInProgressSupplier = indexingInProgressSupplier;

        buildSearchBar();
        buildResults();
        bindActions();
    }

    public Parent getSearchBarView() {
        return searchBarView;
    }

    public Parent getResultsView() {
        VBox box = new VBox(6, statusLabel, resultsList);
        box.setPadding(new Insets(0, 16, 16, 16));
        return box;
    }

    public void refresh() {
        performSearch();
    }

    private void buildSearchBar() {
        searchField.setPromptText("Search content... e.g. hello or \"root path\"");

        searchField.setPrefWidth(300);
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        rankingComboBox.setPrefWidth(160);
        rankingComboBox.setMinWidth(160);

        rankingComboBox.setItems(FXCollections.observableArrayList(
                new PathScoreStrategy(),
                new ModifiedDateStrategy(),
                new AlphabeticalStrategy()
        ));

        rankingComboBox.setValue(rankingComboBox.getItems().get(0));

        rankingComboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RankingStrategy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        rankingComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RankingStrategy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        HBox row = new HBox(10, searchField, searchButton, rankingComboBox);
        row.setAlignment(Pos.CENTER_LEFT);

        searchBarView.getChildren().addAll(row, statusLabel);
        searchBarView.setPadding(new Insets(0, 16, 10, 16));
        searchBarView.setSpacing(10);
    }

    private void buildResults() {
        resultsList.setCellFactory(list -> new SearchResultCell());
        resultsList.setPlaceholder(new Label("No results yet."));
    }

    private void bindActions() {
        searchButton.setOnAction(event -> performSearch());
        searchField.setOnAction(event -> performSearch());
        rankingComboBox.setOnAction(event -> performSearch());

        ChangeListener<String> liveSearchListener = (obs, oldValue, newValue) -> performSearch();
        searchField.textProperty().addListener(liveSearchListener);

        resultsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SearchResult selected = resultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showResultDetails(selected);
                }
            }
        });
    }

    private void performSearch() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            resultsList.setItems(FXCollections.observableArrayList());
            statusLabel.setText("No results yet.");
            return;
        }

        try {
            List<SearchResult> results = searchService.search(
                    query,
                    rootPathSupplier.get(),
                    rankingComboBox.getValue()
            );

            resultsList.setItems(FXCollections.observableArrayList(results));

            String suffix = indexingInProgressSupplier.get()
                    ? " (indexing in progress... results may update)"
                    : "";

            if (results.isEmpty()) {
                statusLabel.setText("No results found for: " + query + suffix);
            } else {
                statusLabel.setText(results.size() + " result(s) for: " + query + suffix);
            }

        } catch (Exception e) {
            resultsList.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Search failed: " + e.getMessage());
        }
    }

    private void showResultDetails(SearchResult result) {
        SearchResultDetailsDialog dialog = new SearchResultDetailsDialog(stage, searchField.getText());
        dialog.show(result);
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }
    public ComboBox<RankingStrategy> getRankingComboBox() {
        return rankingComboBox;
    }

}