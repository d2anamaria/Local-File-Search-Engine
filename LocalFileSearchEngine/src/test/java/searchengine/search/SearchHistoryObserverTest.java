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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchHistoryObserverTest {

    private Connection connection;
    private SearchService searchService;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        new SchemaInitializer().initializeSchema(connection);

        SearchRepository searchRepository = new SearchRepository(connection);
        searchService = new SearchService(searchRepository, new IndexingRules());

        SearchHistoryRepository historyRepository = new SearchHistoryRepository(connection);
        SearchHistoryService historyService = new SearchHistoryService(historyRepository);

        searchService.addSearchObserver(historyService);
    }

    @Test
        // recordSearchHistoryNotifiesObserver:
        // Verifies that SearchService notifies the SearchHistoryService observer
        // and stores the query in the search_history table.
    void recordSearchHistoryNotifiesObserver() throws Exception {
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

    @Test
        // repeatedSearchIncreasesCount:
        // Verifies that recording the same query multiple times updates the existing row
        // instead of inserting duplicates.
    void repeatedSearchIncreasesCount() throws Exception {
        searchService.recordSearchHistory("hello");
        searchService.recordSearchHistory("hello");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT COUNT(*) AS rows, count
                 FROM search_history
                 WHERE query_text = 'hello'
             """)) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt("rows"));
            assertTrue(rs.getDouble("count") > 1.0);
        }
    }

    @Test
        // suggestionsUsePrefix:
        // Verifies that suggestions only return queries matching the typed prefix.
    void suggestionsUsePrefix() {
        searchService.recordSearchHistory("hello world");
        searchService.recordSearchHistory("help me");
        searchService.recordSearchHistory("java file");

        List<String> suggestions = searchService.findSuggestions("he");

        assertTrue(suggestions.contains("hello world"));
        assertTrue(suggestions.contains("help me"));
        assertFalse(suggestions.contains("java file"));
    }

    @Test
        // suggestionsAreRankedByFrequency:
        // Verifies that more frequently used queries are suggested before less used ones.
    void suggestionsAreRankedByFrequency() {
        searchService.recordSearchHistory("hello world");

        searchService.recordSearchHistory("help me");
        searchService.recordSearchHistory("help me");
        searchService.recordSearchHistory("help me");

        List<String> suggestions = searchService.findSuggestions("he");

        assertEquals("help me", suggestions.get(0));
    }

    @Test
        // emptyQueriesAreNotStored:
        // Verifies that blank or invalid queries are ignored and not saved to history.
    void emptyQueriesAreNotStored() throws Exception {
        searchService.recordSearchHistory("   ");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT COUNT(*) AS count
                 FROM search_history
             """)) {

            assertTrue(rs.next());
            assertEquals(0, rs.getInt("count"));
        }
    }
}