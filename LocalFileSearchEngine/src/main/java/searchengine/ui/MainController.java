package searchengine.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import searchengine.config.IndexingRules;
import searchengine.indexer.Indexer;
import searchengine.search.SearchService;

public class MainController {

    private final BorderPane root = new BorderPane();

    private final SearchController searchController;
    private final IndexingController indexingController;
    private final IndexingConfigPanel indexingConfigPanel;

    private String selectedRootPath;

    public MainController(
            SearchService searchService,
            Indexer indexer,
            IndexingRules indexingRules,
            Stage stage
    ) {
        this.searchController = new SearchController(
                searchService,
                stage,
                this::getSelectedRootPath
        );

        this.indexingController = new IndexingController(
                indexer,
                stage,
                this::getSelectedRootPath,
                this::setSelectedRootPath,
                this::handleIndexingFinished
        );

        this.indexingConfigPanel = new IndexingConfigPanel(
                indexingRules,
                this::handleConfigChanged
        );

        buildUi();
    }

    public Parent getView() {
        return root;
    }

    private Parent buildTitleBox() {
        Label titleLabel = new Label("Local File Search");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        return titleLabel;
    }

    public String getSelectedRootPath() {
        return selectedRootPath;
    }

    public void setSelectedRootPath(String selectedRootPath) {
        this.selectedRootPath = selectedRootPath;
    }

    private void buildUi() {
        indexingController.setConfigContent(indexingConfigPanel.getView());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        ColumnConstraints fieldColumn = new ColumnConstraints();

        ColumnConstraints buttonColumn1 = new ColumnConstraints();
        ColumnConstraints buttonColumn2 = new ColumnConstraints();

        form.getColumnConstraints().addAll(fieldColumn, buttonColumn1, buttonColumn2);

        TextField pathField = indexingController.getPathField();
        TextField searchField = searchController.getSearchField();

        pathField.setPrefWidth(500);
        searchField.setPrefWidth(500);

        pathField.setMaxWidth(500);
        searchField.setMaxWidth(500);

        form.add(pathField, 0, 0);
        form.add(indexingController.getBrowseButton(), 1, 0);
        form.add(indexingController.getReindexButton(), 2, 0);

        form.add(searchField, 0, 1);
        form.add(searchController.getSearchButton(), 1, 1);

        VBox topContent = new VBox(
                10,
                buildTitleBox(),
                form,
                indexingController.getStatusView()
        );
        topContent.setPadding(new Insets(16));

        root.setTop(topContent);
        root.setLeft(indexingController.getLeftView());
        root.setCenter(searchController.getResultsView());
    }

    private void handleConfigChanged() {
        indexingController.showConfigUpdatedMessageIfIdle();
        searchController.refresh();
    }

    private void handleIndexingFinished() {
        searchController.refresh();
    }
}