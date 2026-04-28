package searchengine.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.config.IndexingRules;
import searchengine.db.infrastructure.SchemaInitializer;
import searchengine.db.repository.SearchHistoryRepository;
import searchengine.db.repository.SearchRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceInteractionTest {

    private Connection connection;
    private SearchService searchService;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        new SchemaInitializer().initializeSchema(connection);

        SearchRepository searchRepository = new SearchRepository(connection);
        searchService = new SearchService(searchRepository, new IndexingRules());
    }

    // Verifies that calling recordResultClick() inserts/updates a row in result_interactions
    // and correctly increments click_count while leaving copy_path_count unchanged.
    @Test
    void recordResultClickStoresClickInDatabase() throws Exception {
        SearchResult result = new SearchResult(
                "Main.java",
                "/project/src/Main.java",
                "preview",
                "2026-01-01T10:00:00",
                1.0,
                0.0
        );

        searchService.recordResultClick(result, "hello");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT click_count, copy_path_count
                 FROM result_interactions
                 WHERE path = '/project/src/Main.java'
             """)) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt("click_count"));
            assertEquals(0, rs.getInt("copy_path_count"));
        }
    }


    // Verifies that calling recordCopyPath() inserts/updates a row in result_interactions
    // and correctly increments copy_path_count while leaving click_count unchanged.
    @Test
    void recordCopyPathStoresCopyPathInDatabase() throws Exception {
        SearchResult result = new SearchResult(
                "Main.java",
                "/project/src/Main.java",
                "preview",
                "2026-01-01T10:00:00",
                1.0,
                0.0
        );

        searchService.recordCopyPath(result, "hello");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT click_count, copy_path_count
                 FROM result_interactions
                 WHERE path = '/project/src/Main.java'
             """)) {

            assertTrue(rs.next());
            assertEquals(0, rs.getInt("click_count"));
            assertEquals(1, rs.getInt("copy_path_count"));
        }
    }

    // Verifies that when a result is clicked, all extracted query terms are stored
    // in term_file_interactions for that file (learning user behavior per term).
    @Test
    void recordResultClickStoresAllQueryTermsForClickedFile() throws Exception {
        SearchResult result = new SearchResult(
                "Main.java",
                "/project/src/Main.java",
                "preview",
                "2026-01-01T10:00:00",
                1.0,
                0.0
        );

        searchService.recordResultClick(result, "hello world");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
             SELECT COUNT(*) AS count
             FROM term_file_interactions
             WHERE path = '/project/src/Main.java'
               AND term IN ('hello', 'world')
         """)) {

            assertTrue(rs.next());
            assertEquals(2, rs.getInt("count"));
        }
    }

    // Verifies that recordSearchHistory() notifies observers (SearchHistoryService)
    // and results in the query being persisted in the search_history table.
    @Test
    void recordSearchHistoryNotifiesObserverAndStoresQuery() throws Exception {
        SearchHistoryRepository historyRepository = new SearchHistoryRepository(connection);
        SearchHistoryService historyService = new SearchHistoryService(historyRepository);

        searchService.addSearchObserver(historyService);

        searchService.recordSearchHistory("hello");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
             SELECT query_text, count
             FROM search_history
             WHERE query_text = 'hello'
         """)) {

            assertTrue(rs.next());
            assertEquals("hello", rs.getString("query_text"));
            assertTrue(rs.getDouble("count") >= 1.0);
        }
    }
}