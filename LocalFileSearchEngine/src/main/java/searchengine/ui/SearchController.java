package searchengine.ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import searchengine.config.IndexingRules;
import searchengine.indexer.Indexer;
import searchengine.indexer.IndexingProgressListener;
import searchengine.indexer.IndexingResult;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class SearchController {

    private final SearchService searchService;
    private final Indexer indexer;
    private final IndexingRules indexingRules;
    private final Stage stage;

    private final IndexingConfigPanel indexConfigPanel;

    private String selectedRootPath;

    private final BorderPane root = new BorderPane();

    private final TextField pathField = new TextField();
    private final Button browseButton = new Button("Browse");

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button reindexButton = new Button("Reindex");

    private final Button configButton = new Button("⚙ ▶");

    private final Label statusLabel = new Label("Choose a folder, then search.");
    private final ProgressBar progressBar = new ProgressBar();
    private final Button reportButton = new Button("View report");

    private final ListView<SearchResult> resultsList = new ListView<>();

    private final BorderPane leftSide = new BorderPane();
    private final ScrollPane configScrollPane = new ScrollPane();

    private boolean configVisible = false;
    private boolean indexingInProgress = false;

    private IndexingResult lastIndexingResult;

    public SearchController(
            SearchService searchService,
            Indexer indexer,
            IndexingRules indexingRules,
            Stage stage
    ) {
        this.searchService = searchService;
        this.indexer = indexer;
        this.indexingRules = indexingRules;
        this.stage = stage;

        this.indexConfigPanel = new IndexingConfigPanel(indexingRules, this::handleConfigChanged);

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

        HBox pathBar = new HBox(10, pathField, browseButton, reindexButton);
        pathBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search content... e.g. hello or \"root path\"");
        searchField.setPrefWidth(500);

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        progressBar.setVisible(false);
        progressBar.setManaged(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        reportButton.setVisible(false);
        reportButton.setManaged(false);

        HBox statusRow = new HBox(10, statusLabel, reportButton);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(10, titleLabel, pathBar, searchBar, statusRow, progressBar);
        topBox.setPadding(new Insets(16));

        resultsList.setCellFactory(list -> new SearchResultCell());
        resultsList.setPlaceholder(new Label("No results yet."));

        buildLeftSide();

        root.setTop(topBox);
        root.setLeft(leftSide);
        root.setCenter(resultsList);

        BorderPane.setMargin(resultsList, new Insets(0, 16, 16, 16));
    }

    private void buildLeftSide() {
        configButton.setMaxWidth(Double.MAX_VALUE);
        configButton.setStyle("-fx-font-weight: bold;");
        BorderPane.setMargin(configButton, new Insets(10, 10, 10, 10));

        configScrollPane.setContent(indexConfigPanel.getView());
        configScrollPane.setFitToWidth(true);
        configScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        configScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        configScrollPane.setPrefWidth(280);
        configScrollPane.setMinWidth(280);
        configScrollPane.setStyle("-fx-background: #f7f7f7; -fx-background-color: #f7f7f7;");

        configScrollPane.setVisible(false);
        configScrollPane.setManaged(false);

        leftSide.setTop(configButton);
        leftSide.setCenter(configScrollPane);
        leftSide.setPrefWidth(60);
        leftSide.setMinWidth(60);
        leftSide.setStyle("""
                -fx-background-color: #f7f7f7;
                -fx-border-color: #d9d9d9;
                -fx-border-width: 0 1 0 0;
                """);
    }

    private void bindActions() {
        browseButton.setOnAction(event -> chooseDirectory());
        searchButton.setOnAction(event -> performSearch());
        reindexButton.setOnAction(event -> performIndex());
        searchField.setOnAction(event -> performSearch());

        configButton.setOnAction(event -> toggleConfigPanel());
        reportButton.setOnAction(event -> showReportDialog());

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

    private void handleConfigChanged() {
        if (!indexingInProgress) {
            statusLabel.setText("Config updated. Click Reindex to fully apply.");
        }

        performSearch();
    }

    private void toggleConfigPanel() {
        configVisible = !configVisible;
        configScrollPane.setVisible(configVisible);
        configScrollPane.setManaged(configVisible);

        if (configVisible) {
            leftSide.setPrefWidth(300);
            leftSide.setMinWidth(300);
            configButton.setText("◀ ⚙ Config");
        } else {
            leftSide.setPrefWidth(60);
            leftSide.setMinWidth(60);
            configButton.setText("⚙ ▶");
        }
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Index");

        File selectedDirectory = chooser.showDialog(stage);

        if (selectedDirectory != null) {
            selectedRootPath = selectedDirectory.getAbsolutePath();

            pathField.setText(selectedRootPath);

            if (!indexingInProgress) {
                statusLabel.setText("Selected folder: " + selectedRootPath);
            }

            performIndex();
        }
    }

    private void performIndex() {
        String pathText = pathField.getText();

        if (pathText == null || pathText.isBlank()) {
            statusLabel.setText("Please choose a folder first.");
            return;
        }

        if (indexingInProgress) {
            statusLabel.setText("Indexing is already running...");
            return;
        }

        indexingInProgress = true;
        lastIndexingResult = null;

        reportButton.setVisible(false);
        reportButton.setManaged(false);

        browseButton.setDisable(true);
        reindexButton.setDisable(true);

        Task<IndexingResult> task = new Task<>() {
            @Override
            protected IndexingResult call() {
                return indexer.index(Path.of(pathText), new IndexingProgressListener() {
                    @Override
                    public void onCrawlingStarted() {
                        updateMessage("Crawling...");
                        updateProgress(-1, 1);
                    }

                    @Override
                    public void onCrawlingFinished(int totalFiles) {
                        updateMessage("Indexing... 0 / " + totalFiles);
                        updateProgress(0, Math.max(totalFiles, 1));
                    }

                    @Override
                    public void onIndexingProgress(int current, int total) {
                        updateMessage("Indexing... " + current + " / " + total);
                        updateProgress(current, Math.max(total, 1));
                    }
                });
            }
        };

        statusLabel.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        progressBar.setVisible(true);
        progressBar.setManaged(true);

        task.setOnSucceeded(event -> {
            statusLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();

            lastIndexingResult = task.getValue();

            statusLabel.setText("Indexing finished.");
            progressBar.setVisible(false);
            progressBar.setManaged(false);

            reportButton.setVisible(true);
            reportButton.setManaged(true);

            browseButton.setDisable(false);
            reindexButton.setDisable(false);

            indexingInProgress = false;
        });

        task.setOnFailed(event -> {
            statusLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();

            Throwable error = task.getException();
            statusLabel.setText("Indexing failed: " + (error != null ? error.getMessage() : "unknown error"));

            progressBar.setVisible(false);
            progressBar.setManaged(false);

            browseButton.setDisable(false);
            reindexButton.setDisable(false);

            indexingInProgress = false;
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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

            if (!indexingInProgress) {
                if (results.isEmpty()) {
                    statusLabel.setText("No results found for: " + query);
                } else {
                    statusLabel.setText(results.size() + " result(s) for: " + query);
                }
            }
        } catch (Exception e) {
            resultsList.setItems(FXCollections.observableArrayList());

            if (!indexingInProgress) {
                statusLabel.setText("Search failed: " + e.getMessage());
            }
        }
    }

    private void showReportDialog() {
        if (lastIndexingResult == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Indexing Report");
        alert.setHeaderText("Indexing completed");
        alert.setContentText(lastIndexingResult.toDisplayText());
        alert.showAndWait();
    }

    private void showResultDetails(SearchResult result) {
        SearchResultDetailsDialog dialog = new SearchResultDetailsDialog(stage, searchField.getText());
        dialog.show(result);
    }
}