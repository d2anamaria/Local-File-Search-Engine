package searchengine.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import searchengine.config.IndexingRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IndexingConfigPanel {

    private final IndexingRules indexingRules;
    private final Runnable onConfigChanged;

    private final VBox root = new VBox(12);

    private final ListView<String> ignoredDirectoriesList = new ListView<>();
    private final ListView<String> ignoredFileNamesList = new ListView<>();

    public IndexingConfigPanel(IndexingRules indexingRules, Runnable onConfigChanged) {
        this.indexingRules = indexingRules;
        this.onConfigChanged = onConfigChanged;

        buildUi();
    }

    public VBox getView() {
        return root;
    }

    private void buildUi() {
        root.getChildren().clear();
        root.setPadding(new Insets(14));
        root.setSpacing(12);
        root.setFillWidth(true);

        Label configTitle = new Label("Runtime configuration");
        configTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label infoLabel = new Label(
                "Changes affect visible filtering immediately.\n" +
                        "Click Reindex to fully apply them to indexing."
        );
        infoLabel.setWrapText(true);

        TitledPane textFilesPane = buildExtensionsPane();
        TitledPane ignoredDirectoriesPane = buildIgnoredDirectoriesPane();
        TitledPane ignoredFileNamesPane = buildIgnoredFileNamesPane();
        TitledPane indexingPane = buildIndexingOptionsPane();

        root.getChildren().addAll(
                configTitle,
                infoLabel,
                textFilesPane,
                ignoredDirectoriesPane,
                ignoredFileNamesPane,
                indexingPane
        );
    }

    private TitledPane buildExtensionsPane() {
        VBox extensionsBox = new VBox(8);

        List<String> extensions = new ArrayList<>(IndexingRules.DEFAULT_TEXT_EXTENSIONS);
        extensions.sort(Comparator.naturalOrder());

        CheckBox enableAllCheckBox = new CheckBox("Enable all text file types");
        enableAllCheckBox.setSelected(
                indexingRules.getEnabledTextExtensions().containsAll(extensions)
                        && indexingRules.getEnabledTextExtensions().size() == extensions.size()
        );

        List<CheckBox> extensionCheckBoxes = new ArrayList<>();

        for (String extension : extensions) {
            CheckBox checkBox = new CheckBox("." + extension);
            checkBox.setSelected(indexingRules.isExtensionEnabled(extension));

            checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
                indexingRules.setExtensionEnabled(extension, newValue);
                updateEnableAllCheckBox(enableAllCheckBox, extensionCheckBoxes, extensions.size());
                notifyConfigChanged();
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

            notifyConfigChanged();
        });

        extensionsBox.getChildren().add(enableAllCheckBox);
        extensionsBox.getChildren().add(new Separator());
        extensionsBox.getChildren().addAll(extensionCheckBoxes);

        TitledPane pane = new TitledPane("Text files", extensionsBox);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private TitledPane buildIgnoredDirectoriesPane() {
        VBox content = new VBox(8);

        ignoredDirectoriesList.setPrefHeight(120);
        refreshIgnoredDirectoriesList();

        TextField inputField = new TextField();
        inputField.setPromptText("Add directory name, e.g. dist");

        Button addButton = new Button("Add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            String value = inputField.getText();
            if (value == null || value.isBlank()) {
                return;
            }

            indexingRules.addIgnoredDirectory(value);
            inputField.clear();
            refreshIgnoredDirectoriesList();
            notifyConfigChanged();
        });

        Button removeButton = new Button("Remove selected");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(event -> {
            String selected = ignoredDirectoriesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            indexingRules.removeIgnoredDirectory(selected);
            refreshIgnoredDirectoriesList();
            notifyConfigChanged();
        });

        HBox addRow = new HBox(8, inputField, addButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        content.getChildren().addAll(
                new Label("Ignored directory names"),
                ignoredDirectoriesList,
                addRow,
                removeButton
        );

        TitledPane pane = new TitledPane("Ignored directories", content);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private TitledPane buildIgnoredFileNamesPane() {
        VBox content = new VBox(8);

        ignoredFileNamesList.setPrefHeight(120);
        refreshIgnoredFileNamesList();

        TextField inputField = new TextField();
        inputField.setPromptText("Add file name, e.g. notes.tmp");

        Button addButton = new Button("Add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            String value = inputField.getText();
            if (value == null || value.isBlank()) {
                return;
            }

            indexingRules.addIgnoredFileName(value);
            inputField.clear();
            refreshIgnoredFileNamesList();
            notifyConfigChanged();
        });

        Button removeButton = new Button("Remove selected");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(event -> {
            String selected = ignoredFileNamesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            indexingRules.removeIgnoredFileName(selected);
            refreshIgnoredFileNamesList();
            notifyConfigChanged();
        });

        HBox addRow = new HBox(8, inputField, addButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        content.getChildren().addAll(
                new Label("Ignored file names"),
                ignoredFileNamesList,
                addRow,
                removeButton
        );

        TitledPane pane = new TitledPane("Ignored file names", content);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private TitledPane buildIndexingOptionsPane() {
        VBox content = new VBox(10);

        CheckBox includeHiddenFilesCheckBox = new CheckBox("Include hidden files");
        includeHiddenFilesCheckBox.setSelected(indexingRules.isIncludeHiddenFiles());
        includeHiddenFilesCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            indexingRules.setIncludeHiddenFiles(newValue);
            notifyConfigChanged();
        });

        Label maxSizeLabel = new Label("Max indexed file size (MB)");

        TextField maxSizeField = new TextField();
        maxSizeField.setText(String.valueOf(bytesToMegabytes(indexingRules.getMaxIndexedFileSizeBytes())));
        maxSizeField.setPromptText("10");

        Button applyMaxSizeButton = new Button("Apply");
        applyMaxSizeButton.setOnAction(event -> applyMaxSize(maxSizeField));

        maxSizeField.setOnAction(event -> applyMaxSize(maxSizeField));

        HBox sizeRow = new HBox(8, maxSizeField, applyMaxSizeButton);
        HBox.setHgrow(maxSizeField, Priority.ALWAYS);

        Label helpLabel = new Label("Files larger than this are skipped during indexing.");
        helpLabel.setWrapText(true);
        helpLabel.setStyle("-fx-text-fill: #666666;");

        content.getChildren().addAll(
                includeHiddenFilesCheckBox,
                maxSizeLabel,
                sizeRow,
                helpLabel
        );

        TitledPane pane = new TitledPane("Indexing options", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    private void applyMaxSize(TextField maxSizeField) {
        String value = maxSizeField.getText();

        if (value == null || value.isBlank()) {
            maxSizeField.setText(String.valueOf(bytesToMegabytes(indexingRules.getMaxIndexedFileSizeBytes())));
            return;
        }

        try {
            long mb = Long.parseLong(value.trim());

            if (mb < 0) {
                throw new NumberFormatException("negative");
            }

            indexingRules.setMaxIndexedFileSizeBytes(mb * 1024 * 1024);
            maxSizeField.setText(String.valueOf(mb));
            notifyConfigChanged();
        } catch (NumberFormatException e) {
            maxSizeField.setText(String.valueOf(bytesToMegabytes(indexingRules.getMaxIndexedFileSizeBytes())));
        }
    }

    private void refreshIgnoredDirectoriesList() {
        List<String> values = new ArrayList<>(indexingRules.getIgnoredDirectories());
        values.sort(Comparator.naturalOrder());
        ignoredDirectoriesList.getItems().setAll(values);
    }

    private void refreshIgnoredFileNamesList() {
        List<String> values = new ArrayList<>(indexingRules.getIgnoredFileNames());
        values.sort(Comparator.naturalOrder());
        ignoredFileNamesList.getItems().setAll(values);
    }

    private void updateEnableAllCheckBox(
            CheckBox enableAllCheckBox,
            List<CheckBox> extensionCheckBoxes,
            int totalExtensions
    ) {
        long selectedCount = extensionCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .count();

        enableAllCheckBox.setSelected(selectedCount == totalExtensions);
    }

    private long bytesToMegabytes(long bytes) {
        return bytes / (1024 * 1024);
    }

    private void notifyConfigChanged() {
        if (onConfigChanged != null) {
            onConfigChanged.run();
        }
    }
}