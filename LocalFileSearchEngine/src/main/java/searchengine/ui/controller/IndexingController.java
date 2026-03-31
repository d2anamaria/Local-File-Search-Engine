package searchengine.ui.controller;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import searchengine.indexer.Indexer;
import searchengine.indexer.IndexingProgressListener;
import searchengine.indexer.IndexingResult;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IndexingController {

    private final Indexer indexer;
    private final Stage stage;
    private final Supplier<String> rootPathSupplier;
    private final Consumer<String> rootPathConsumer;
    private final Runnable onIndexingFinished;

    private final VBox topView = new VBox(10);
    private final BorderPane leftView = new BorderPane();
    private final VBox statusView = new VBox(5);

    private final TextField pathField = new TextField();
    private final Button browseButton = new Button("Browse");
    private final Button reindexButton = new Button("Reindex");

    private final Button configButton = new Button("⚙ ▶");

    private final Label statusLabel = new Label("Choose a folder, then search.");
    private final ProgressBar progressBar = new ProgressBar();
    private final Button reportButton = new Button("View report");

    private final ScrollPane configScrollPane = new ScrollPane();

    private boolean configVisible = false;
    private boolean indexingInProgress = false;

    private IndexingResult lastIndexingResult;

    public IndexingController(
            Indexer indexer,
            Stage stage,
            Supplier<String> rootPathSupplier,
            Consumer<String> rootPathConsumer,
            Runnable onIndexingFinished
    ) {
        this.indexer = indexer;
        this.stage = stage;
        this.rootPathSupplier = rootPathSupplier;
        this.rootPathConsumer = rootPathConsumer;
        this.onIndexingFinished = onIndexingFinished;

        buildUi();
        bindActions();
    }

    public Parent getTopView() {
        return topView;
    }

    public Parent getLeftView() {
        return leftView;
    }

    public Parent getHeaderView() {
        return topView;
    }

    public Parent getStatusView() {
        return statusView;
    }

    public void setConfigContent(Parent configContent) {
        configScrollPane.setContent(configContent);
    }

    public void showConfigUpdatedMessageIfIdle() {
        if (!indexingInProgress) {
            statusLabel.setText("Config updated. Click Reindex to fully apply.");
        }
    }

    private void buildUi() {
        Label titleLabel = new Label("Local File Search");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        pathField.setPromptText("Choose root folder to index...");
        pathField.setPrefWidth(500);

        HBox pathBar = new HBox(10, pathField, browseButton, reindexButton);
        pathBar.setAlignment(Pos.CENTER_LEFT);

        progressBar.setVisible(false);
        progressBar.setManaged(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        reportButton.setVisible(false);
        reportButton.setManaged(false);

        HBox statusRow = new HBox(10, statusLabel, reportButton);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        topView.getChildren().addAll(titleLabel, pathBar);
        topView.setPadding(new Insets(16));
        topView.setSpacing(10);

        statusView.getChildren().addAll(statusRow, progressBar);
        statusView.setPadding(new Insets(0, 0, 6, 0));
        statusView.setSpacing(5);

        buildLeftSide();
    }

    private void buildLeftSide() {
        configButton.setMaxWidth(Double.MAX_VALUE);
        configButton.setStyle("-fx-font-weight: bold;");
        BorderPane.setMargin(configButton, new Insets(10, 10, 10, 10));

        configScrollPane.setFitToWidth(true);
        configScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        configScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        configScrollPane.setPrefWidth(280);
        configScrollPane.setMinWidth(280);
        configScrollPane.setStyle("-fx-background: #f7f7f7; -fx-background-color: #f7f7f7;");
        configScrollPane.setVisible(false);
        configScrollPane.setManaged(false);

        leftView.setTop(configButton);
        leftView.setCenter(configScrollPane);
        leftView.setPrefWidth(60);
        leftView.setMinWidth(60);
        leftView.setStyle("""
                -fx-background-color: #f7f7f7;
                -fx-border-color: #d9d9d9;
                -fx-border-width: 0 1 0 0;
                """);
    }

    private void bindActions() {
        browseButton.setOnAction(event -> chooseDirectory());
        reindexButton.setOnAction(event -> performIndex());
        configButton.setOnAction(event -> toggleConfigPanel());
        reportButton.setOnAction(event -> showReportDialog());
    }

    private void toggleConfigPanel() {
        configVisible = !configVisible;
        configScrollPane.setVisible(configVisible);
        configScrollPane.setManaged(configVisible);

        if (configVisible) {
            leftView.setPrefWidth(300);
            leftView.setMinWidth(300);
            configButton.setText("◀ ⚙ Config");
        } else {
            leftView.setPrefWidth(60);
            leftView.setMinWidth(60);
            configButton.setText("⚙ ▶");
        }
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Index");

        File selectedDirectory = chooser.showDialog(stage);

        if (selectedDirectory != null) {
            String selectedRootPath = selectedDirectory.getAbsolutePath();

            rootPathConsumer.accept(selectedRootPath);
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
            pathText = rootPathSupplier.get();
        }

        if (pathText == null || pathText.isBlank()) {
            statusLabel.setText("Please choose a folder first.");
            return;
        }

        if (indexingInProgress) {
            statusLabel.setText("Indexing is already running...");
            return;
        }

        rootPathConsumer.accept(pathText);

        indexingInProgress = true;
        lastIndexingResult = null;

        reportButton.setVisible(false);
        reportButton.setManaged(false);

        browseButton.setDisable(true);
        reindexButton.setDisable(true);

        String finalPathText = pathText;

        Task<IndexingResult> task = new Task<>() {
            @Override
            protected IndexingResult call() {
                return indexer.index(Path.of(finalPathText), new IndexingProgressListener() {
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

            if (onIndexingFinished != null) {
                onIndexingFinished.run();
            }
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

    public TextField getPathField() {
        return pathField;
    }

    public Button getBrowseButton() {
        return browseButton;
    }

    public Button getReindexButton() {
        return reindexButton;
    }
}