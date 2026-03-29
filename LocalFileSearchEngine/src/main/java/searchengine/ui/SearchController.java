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
import searchengine.config.IndexingRules;
import searchengine.crawler.CrawlStats;
import searchengine.indexer.Indexer;
import searchengine.search.SearchResult;
import searchengine.search.SearchService;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchController {

    private final SearchService searchService;
    private final Indexer indexer;
    private final IndexingRules indexingRules;
    private final Stage stage;

    private String selectedRootPath;

    private final BorderPane root = new BorderPane();

    private final TextField pathField = new TextField();
    private final Button browseButton = new Button("Browse");

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button reindexButton = new Button("Reindex");

    private final Button configButton = new Button("⚙ ▶");

    private final Label statusLabel = new Label("Choose a folder, then search.");
    private final ListView<SearchResult> resultsList = new ListView<>();

    private final BorderPane leftSide = new BorderPane();
    private final VBox configPanel = new VBox(12);
    private final ScrollPane configScrollPane = new ScrollPane();

    private boolean configVisible = false;
    private boolean updatingFromMaster = false;

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

        VBox topBox = new VBox(10, titleLabel, pathBar, searchBar, statusLabel);
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

        buildConfigPanel();

        configScrollPane.setContent(configPanel);
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

    private void buildConfigPanel() {
        configPanel.getChildren().clear();
        configPanel.setPadding(new Insets(14));
        configPanel.setSpacing(12);
        configPanel.setFillWidth(true);

        Label configTitle = new Label("Runtime configuration");
        configTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label infoLabel = new Label(
                "Checked types will be indexed next time.\n" +
                        "Old indexed files stay in the database."
        );
        infoLabel.setWrapText(true);

        VBox extensionsBox = new VBox(8);

        List<String> extensions = new ArrayList<>(IndexingRules.DEFAULT_TEXT_EXTENSIONS);
        extensions.sort(Comparator.naturalOrder());

        CheckBox enableAllCheckBox = new CheckBox("Enable all text file types");
        enableAllCheckBox.setSelected(true);

        List<CheckBox> extensionCheckBoxes = new ArrayList<>();

        for (String extension : extensions) {
            CheckBox checkBox = new CheckBox("." + extension);
            checkBox.setSelected(indexingRules.isExtensionEnabled(extension));

            checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
                indexingRules.setExtensionEnabled(extension, newValue);
                statusLabel.setText("Config updated. Click Reindex to apply.");
                performSearch();
            });

            extensionCheckBoxes.add(checkBox);
        }

        enableAllCheckBox.setOnAction(event -> {
            boolean selected = enableAllCheckBox.isSelected();

            for (String extension : extensions) {
                indexingRules.setExtensionEnabled(extension, selected);
            }

            for (CheckBox checkBox : extensionCheckBoxes) {
                checkBox.setSelected(selected);
            }

            statusLabel.setText("Config updated. Click Reindex to apply.");
            performSearch();

        });

        extensionsBox.getChildren().add(enableAllCheckBox);
        extensionsBox.getChildren().add(new Separator());

        for (CheckBox checkBox : extensionCheckBoxes) {
            extensionsBox.getChildren().add(checkBox);
        }

        TitledPane textFilesPane = new TitledPane("Text files", extensionsBox);
        textFilesPane.setExpanded(true);
        textFilesPane.setCollapsible(true);
        textFilesPane.setMaxWidth(Double.MAX_VALUE);

        configPanel.getChildren().addAll(configTitle, infoLabel, textFilesPane);
    }


    private void bindActions() {
        browseButton.setOnAction(event -> chooseDirectory());
        searchButton.setOnAction(event -> performSearch());
        reindexButton.setOnAction(event -> performIndex());
        searchField.setOnAction(event -> performSearch());

        configButton.setOnAction(event -> toggleConfigPanel());

        ChangeListener<String> liveSearchListener = (obs, oldValue, newValue) -> performSearch();
        searchField.textProperty().addListener(liveSearchListener);
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