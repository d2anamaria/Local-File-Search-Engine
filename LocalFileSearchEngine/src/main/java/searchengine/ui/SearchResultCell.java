package searchengine.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import searchengine.search.SearchResult;

public class SearchResultCell extends ListCell<SearchResult> {

    @Override
    protected void updateItem(SearchResult item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Label fileNameLabel = new Label(item.getFileName());
        fileNameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label pathLabel = new Label(item.getPath());
        pathLabel.setWrapText(true);
        pathLabel.setStyle("-fx-text-fill: #555555;");

        Label previewLabel = new Label(item.getPreview());
        previewLabel.setWrapText(true);
        previewLabel.setStyle("-fx-text-fill: #222222;");

        VBox content = new VBox(6, fileNameLabel, pathLabel, previewLabel);
        content.setPadding(new Insets(10));
        content.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #dcdcdc;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
        """);

        setText(null);
        setGraphic(content);
    }
}