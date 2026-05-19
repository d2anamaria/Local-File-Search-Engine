package searchengine.search;

import org.junit.jupiter.api.Test;
import searchengine.config.IndexingRules;
import searchengine.db.infrastructure.SchemaInitializer;
import searchengine.db.repository.FileIndexRepository;
import searchengine.db.repository.SearchRepository;
import searchengine.indexer.IndexedFileData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryBehaviorTest {

    @Test
    void searchFindsTextFileUsingRawContentQuery() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            new SchemaInitializer().initializeSchema(connection);

            FileIndexRepository fileRepository = new FileIndexRepository(connection);
            fileRepository.save(createTextFile());

            SearchService searchService = new SearchService(
                    new SearchRepository(connection),
                    new IndexingRules()
            );

            List<SearchResult> results = searchService.search("hello");

            assertFalse(results.isEmpty());
            assertEquals("notes.txt", results.get(0).getFileName());
        }
    }

    @Test
    void searchSupportsPathQualifierTogetherWithContent() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            new SchemaInitializer().initializeSchema(connection);

            FileIndexRepository fileRepository = new FileIndexRepository(connection);
            fileRepository.save(createTextFile());

            SearchService searchService = new SearchService(
                    new SearchRepository(connection),
                    new IndexingRules()
            );

            List<SearchResult> results = searchService.search("content:hello path:docs");

            assertFalse(results.isEmpty());
            assertEquals("/project/docs/notes.txt", results.get(0).getPath());
        }
    }

    private IndexedFileData createTextFile() {
        return new IndexedFileData(
                "/project/docs/notes.txt",
                "notes.txt",
                "txt",
                "text/plain",
                100,
                Instant.now().toString(),
                Instant.now().toString(),
                Instant.now().toString(),
                "hash-notes",
                false,
                "text",
                null,
                "hello world from search engine",
                "hello world from search engine",
                1,
                10.0,
                0.0,
                0.0,
                0.0
        );
    }

}