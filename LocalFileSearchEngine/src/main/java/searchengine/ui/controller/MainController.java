package searchengine.ui.controller;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import searchengine.config.IndexingRules;
import searchengine.indexer.Indexer;
import searchengine.search.SearchService;
import searchengine.ui.component.IndexingConfigPanel;

public class MainController {

    private final BorderPane root = new BorderPane();

    private final SearchController searchController;
    private final IndexingController indexingController;
    private final IndexingConfigPanel indexingConfigPanel;

    private String selectedRootPath;

    private boolean indexingInProgress;

    public MainController(
            SearchService searchService,
            Indexer indexer,
            IndexingRules indexingRules,
            Stage stage
    ) {
        this.searchController = new SearchController(
                searchService,
                stage,
                this::getSelectedRootPath,
                this::isIndexingInProgress
        );

        this.indexingController = new IndexingController(
                indexer,
                stage,
                this::getSelectedRootPath,
                this::setSelectedRootPath,
                this::handleIndexingStarted,
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

        TextField pathField = indexingController.getPathField();
        TextField searchField = searchController.getSearchField();

        // keep compact width (like before)
        pathField.setPrefWidth(500);
        pathField.setMaxWidth(500);

        searchField.setPrefWidth(500);
        searchField.setMaxWidth(500);

        searchController.getRankingComboBox().setPrefWidth(180);
        searchController.getRankingComboBox().setMaxWidth(180);

        HBox indexRow = new HBox(
                8,
                pathField,
                indexingController.getBrowseButton(),
                indexingController.getReindexButton(),
                indexingController.getStopButton()
        );
        indexRow.setAlignment(Pos.CENTER_LEFT);

        HBox searchRow = new HBox(
                8,
                searchField,
                searchController.getSearchButton(),
                searchController.getRankingComboBox()
        );
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox topContent = new VBox(
                10,
                buildTitleBox(),
                indexRow,
                searchRow,
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

    private void handleIndexingStarted() {
        indexingInProgress = true;
        searchController.refresh();
    }

    private void handleIndexingFinished() {
        indexingInProgress = false;
        searchController.refresh();
    }

    public boolean isIndexingInProgress() {
        return indexingInProgress;
    }
}