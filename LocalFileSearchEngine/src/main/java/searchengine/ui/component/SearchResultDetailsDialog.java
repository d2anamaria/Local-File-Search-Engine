package searchengine.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import searchengine.search.SearchResult;
import searchengine.ui.util.TextHighlighter;

import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SearchResultDetailsDialog {

    private final Stage owner;
    private final String query;
    private final Runnable onCopyPath;

    public SearchResultDetailsDialog(Stage owner, String query, Runnable onCopyPath) {
        this.owner = owner;
        this.query = query;
        this.onCopyPath = onCopyPath;
    }

    public void show(SearchResult result) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(result.getFileName());

        Label fileNameLabel = new Label(result.getFileName());
        fileNameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label pathTitleLabel = new Label("Path");
        pathTitleLabel.setStyle("-fx-font-weight: bold;");

        TextArea pathArea = new TextArea(result.getPath());
        pathArea.setWrapText(true);
        pathArea.setEditable(false);
        pathArea.setPrefRowCount(2);

        Label previewTitleLabel = new Label("Preview");
        previewTitleLabel.setStyle("-fx-font-weight: bold;");

        TextArea previewArea = new TextArea(result.getPreview());
        previewArea.setWrapText(true);
        previewArea.setEditable(false);
        previewArea.setPrefRowCount(4);

        Label contentTitleLabel = new Label("Full text");
        contentTitleLabel.setStyle("-fx-font-weight: bold;");

        String fullText = loadFullText(result.getPath());

        TextFlow contentFlow = TextHighlighter.buildHighlightedTextFlow(fullText, query);
        contentFlow.setPrefWidth(740);

        ScrollPane contentScrollPane = new ScrollPane(contentFlow);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setPannable(true);
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);

        Button copyPathButton = new Button("Copy path");
        copyPathButton.setOnAction(event -> {
            copyPath(result.getPath());

            if (onCopyPath != null) {
                onCopyPath.run();
            }
        });

        Button openFolderButton = new Button("Open containing folder");
        openFolderButton.setOnAction(event -> {
            openContainingFolder(result.getPath());

            if (onCopyPath != null) {
                onCopyPath.run();
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> dialog.close());

        HBox buttonBar = new HBox(10, copyPathButton, openFolderButton, closeButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(
                10,
                fileNameLabel,
                pathTitleLabel,
                pathArea,
                previewTitleLabel,
                previewArea,
                contentTitleLabel,
                contentScrollPane,
                buttonBar
        );
        layout.setPadding(new Insets(16));
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);

        Scene scene = new Scene(layout, 800, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private String loadFullText(String path) {
        try {
            Path filePath = Path.of(path);

            if (!Files.exists(filePath)) {
                return "File no longer exists on disk.";
            }

            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Could not load full text: " + e.getMessage();
        }
    }

    private void copyPath(String path) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(path);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void openContainingFolder(String path) {
        try {
            File file = Path.of(path).toFile();
            File parent = file.getParentFile();

            if (parent == null || !parent.exists()) {
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                return;
            }

            Desktop.getDesktop().open(parent);
        } catch (Exception ignored) {
        }
    }
}