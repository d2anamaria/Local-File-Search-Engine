package searchengine.ui.controller;

import javafx.animation.PauseTransition;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;
import searchengine.ui.component.SearchResultCell;
import searchengine.ui.component.SearchResultDetailsDialog;
import searchengine.ranking.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SearchController {

    private final SearchService searchService;
    private final Stage stage;
    private final Supplier<String> rootPathSupplier;

    private final VBox searchBarView = new VBox();
    private final ListView<SearchResult> resultsList = new ListView<>();
    private final ListView<String> suggestionsList = new ListView<>();

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Label statusLabel = new Label("No results yet.");

    private final ComboBox<RankingStrategy> rankingComboBox = new ComboBox<>();
    private final Popup suggestionsPopup = new Popup();
    private final PauseTransition historyDelay = new PauseTransition(Duration.seconds(3));
    private boolean applyingSuggestion = false;

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
        buildSuggestionsPopup();
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

        rankingComboBox.setItems(
                FXCollections.observableArrayList(RankingStrategyRegistry.getAvailableStrategies())
        );

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

        searchBarView.getChildren().addAll(row);
        searchBarView.setPadding(new Insets(0, 16, 10, 16));
        searchBarView.setSpacing(10);
    }

    private void buildResults() {
        resultsList.setCellFactory(list -> new SearchResultCell());
        resultsList.setPlaceholder(new Label("No results yet."));
        resultsList.setFixedCellSize(120);
    }

    private void buildSuggestionsPopup() {
        suggestionsList.setPrefWidth(500);
        suggestionsList.setMaxHeight(120);

        suggestionsPopup.getContent().add(suggestionsList);
        suggestionsPopup.setAutoHide(true);

        suggestionsList.setOnMouseClicked(event -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                applySuggestion(selected);
            }
        });

        suggestionsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selected = suggestionsList.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    applySuggestion(selected);
                    event.consume();
                }
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                suggestionsPopup.hide();
                searchField.requestFocus();
                event.consume();
            }
        });
    }

    private void bindActions() {
        searchButton.setOnAction(event -> performSearch());
        searchField.setOnAction(event -> performSearch());
        rankingComboBox.setOnAction(event -> performSearch());

        ChangeListener<String> liveSearchListener = (obs, oldValue, newValue) -> {
            if (applyingSuggestion) {
                return;
            }

            updateSuggestions(newValue);
            performSearch();

            historyDelay.stop();
            historyDelay.setOnFinished(event -> {
                String finalQuery = searchField.getText();
                searchService.recordSearchHistory(finalQuery);
            });
            historyDelay.playFromStart();
        };

        searchField.textProperty().addListener(liveSearchListener);

        searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN && suggestionsPopup.isShowing()) {
                suggestionsList.requestFocus();
                suggestionsList.getSelectionModel().selectFirst();
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.SPACE) {
                suggestionsPopup.hide();
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                suggestionsPopup.hide();
                event.consume();
            }
        });


        resultsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SearchResult selected = resultsList.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    searchService.recordResultClick(selected, searchField.getText());
                    showResultDetails(selected);
                }
            }
        });

        searchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                suggestionsPopup.hide();
            }
        });
    }

    private void updateSuggestions(String text) {
        suggestionsPopup.hide();
        suggestionsList.getItems().clear();

        if (text == null || text.isBlank() || text.trim().length() < 2) {
            return;
        }

        if (text.endsWith(" ")) {
            return;
        }

        String currentText = text.trim();

        List<String> suggestions = new ArrayList<>(
                searchService.findSuggestions(currentText)
        );

        suggestions.remove(currentText);
        suggestions.add(0, currentText);

        if (suggestions.isEmpty() || !searchField.isFocused()) {
            return;
        }

        suggestionsList.getItems().setAll(suggestions);

        double x = searchField.localToScreen(0, 0).getX();
        double y = searchField.localToScreen(0, searchField.getHeight()).getY();

        suggestionsPopup.show(searchField, x, y);
    }

    private void applySuggestion(String suggestion) {
        applyingSuggestion = true;

        suggestionsPopup.hide();
        historyDelay.stop();

        searchField.setText(suggestion);
        searchField.positionCaret(suggestion.length());
        searchField.requestFocus();

        applyingSuggestion = false;

        performSearch();
        searchService.recordSearchHistory(suggestion);
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
        SearchResultDetailsDialog dialog = new SearchResultDetailsDialog(
                stage,
                searchField.getText(),
                () -> searchService.recordCopyPath(result, searchField.getText())
        );

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