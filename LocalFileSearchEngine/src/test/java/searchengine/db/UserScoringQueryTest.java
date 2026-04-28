package searchengine.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.config.IndexingRules;
import searchengine.db.infrastructure.SchemaInitializer;
import searchengine.db.repository.SearchRepository;
import searchengine.db.sql.*;
import searchengine.search.QueryParser;
import searchengine.search.SearchQuery;
import searchengine.search.SearchResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserScoringQueryTest {

    private Connection connection;
    private SearchRepository repository;
    private IndexingRules rules;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        new SchemaInitializer().initializeSchema(connection);

        repository = new SearchRepository(connection);
        rules = new IndexingRules();

        insertFile(
                "/project/src/Main.java",
                "Main.java",
                "java",
                "hello world",
                5.0
        );

        insertFile(
                "/project/docs/readme.txt",
                "readme.txt",
                "txt",
                "hello world",
                1.0
        );
    }

    @Test
    void resultWithClickHasHigherUserRelevanceScore() throws Exception {
        insertResultInteraction("/project/docs/readme.txt", 10, 0);

        SearchQuery query = new QueryParser().parse("hello");

        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult clicked = findByFileName(results, "readme.txt");
        SearchResult notClicked = findByFileName(results, "Main.java");

        assertTrue(clicked.getUserRelevanceScore() > notClicked.getUserRelevanceScore());
    }

    // Verifies that a stored term-file pair boosts the file when the same query term is searched.
    @Test
    void termFileInteractionBoostsMatchingFile() throws Exception {
        insertTermFileInteraction("hello", "/project/docs/readme.txt", 10.0);

        SearchQuery query = new QueryParser().parse("hello");

        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult boosted = findByFileName(results, "readme.txt");
        SearchResult normal = findByFileName(results, "Main.java");

        assertTrue(boosted.getUserRelevanceScore() > normal.getUserRelevanceScore());
    }

    // Verifies that copy-path interactions increase a file’s relevance score
    // compared to a file with no user interaction.
    @Test
    void copyPathInteractionBoostsFileMoreThanNoInteraction() throws Exception {
        insertResultInteraction("/project/docs/readme.txt", 0, 5);

        SearchQuery query = new QueryParser().parse("hello");
        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult copied = findByFileName(results, "readme.txt");
        SearchResult normal = findByFileName(results, "Main.java");

        assertTrue(copied.getUserRelevanceScore() > normal.getUserRelevanceScore());
    }

    // Verifies that copy-path interactions have a stronger influence on relevance
    // than click interactions when both have the same count.
    @Test
    void copyPathHasStrongerWeightThanClickForSameCount() throws Exception {
        insertResultInteraction("/project/docs/readme.txt", 0, 5);
        insertResultInteraction("/project/src/Main.java", 5, 0);

        SearchQuery query = new QueryParser().parse("hello");
        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult copied = findByFileName(results, "readme.txt");
        SearchResult clicked = findByFileName(results, "Main.java");

        assertTrue(copied.getUserRelevanceScore() > clicked.getUserRelevanceScore());
    }

    // Verifies that files with extensions frequently interacted with by the user
    // receive a relevance boost (dynamic extension preference).
    @Test
    void extensionPreferenceBoostsFilesWithFrequentlyInteractedExtension() throws Exception {
        insertFile(
                "/project/other/OldJavaFile.java",
                "OldJavaFile.java",
                "java",
                "different content",
                1.0
        );

        insertResultInteraction("/project/other/OldJavaFile.java", 20, 0);

        SearchQuery query = new QueryParser().parse("hello");
        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult javaResult = findByFileName(results, "Main.java");
        SearchResult txtResult = findByFileName(results, "readme.txt");

        assertTrue(javaResult.getUserRelevanceScore() > txtResult.getUserRelevanceScore());
    }

    // Verifies that recently modified files receive a higher relevance score
    // compared to older files with similar content and base scores.
    @Test
    void recentlyModifiedFileGetsRecencyBoost() throws Exception {
        insertFileWithModifiedAt(
                "/project/recent/recent.txt",
                "recent.txt",
                "txt",
                "hello world",
                1.0,
                java.time.LocalDateTime.now().toString()
        );

        insertFileWithModifiedAt(
                "/project/old/old.txt",
                "old.txt",
                "txt",
                "hello world",
                1.0,
                "2020-01-01T10:00:00"
        );

        SearchQuery query = new QueryParser().parse("hello");
        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult recent = findByFileName(results, "recent.txt");
        SearchResult old = findByFileName(results, "old.txt");

        assertTrue(recent.getUserRelevanceScore() > old.getUserRelevanceScore());
    }

    // Verifies that files with no user interaction still receive a valid
    // (non-negative) relevance score and are not excluded or broken.
    @Test
    void fileWithNoInteractionsStillGetsValidNonNegativeScore() {
        SearchQuery query = new QueryParser().parse("hello");
        List<SearchResult> results = repository.searchByContent(query, rules);

        SearchResult result = findByFileName(results, "Main.java");

        assertTrue(result.getUserRelevanceScore() >= 0);
    }

    // helper methods
    private void insertFile(
            String path,
            String fileName,
            String extension,
            String content,
            double pathScore
    ) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(FileSql.INSERT_OR_REPLACE_FILE)) {
            ps.setString(1, path);
            ps.setString(2, fileName);
            ps.setString(3, extension);
            ps.setString(4, "text/plain");
            ps.setLong(5, 100);
            ps.setString(6, "2026-01-01T10:00:00");
            ps.setString(7, "2026-01-01T10:00:00");
            ps.setString(8, "2026-01-01T10:00:00");
            ps.setString(9, "hash-" + fileName);
            ps.setInt(10, 0);
            ps.setInt(11, 1);
            ps.setInt(12, 2);
            ps.setDouble(13, 0);
            ps.setDouble(14, 0);
            ps.setDouble(15, 0);
            ps.setDouble(16, pathScore);
            ps.setString(17, content);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(FileSql.INSERT_FTS_ROW)) {
            ps.setString(1, fileName);
            ps.setString(2, path);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    private void insertResultInteraction(
            String path,
            int clickCount,
            int copyPathCount
    ) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("""
            INSERT INTO result_interactions(path, click_count, copy_path_count, last_interacted_at)
            VALUES (?, ?, ?, ?)
        """)) {
            ps.setString(1, path);
            ps.setInt(2, clickCount);
            ps.setInt(3, copyPathCount);
            ps.setString(4, "2026-01-01T10:00:00");
            ps.executeUpdate();
        }
    }

    private void insertTermFileInteraction(
            String term,
            String path,
            double score
    ) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("""
            INSERT INTO term_file_interactions(term, path, score, last_used)
            VALUES (?, ?, ?, ?)
        """)) {
            ps.setString(1, term);
            ps.setString(2, path);
            ps.setDouble(3, score);
            ps.setString(4, "2026-01-01T10:00:00");
            ps.executeUpdate();
        }
    }

    private SearchResult findByFileName(List<SearchResult> results, String fileName) {
        return results.stream()
                .filter(result -> result.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow();
    }

    private void insertFileWithModifiedAt(
            String path,
            String fileName,
            String extension,
            String content,
            double pathScore,
            String modifiedAt
    ) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(FileSql.INSERT_OR_REPLACE_FILE)) {
            ps.setString(1, path);
            ps.setString(2, fileName);
            ps.setString(3, extension);
            ps.setString(4, "text/plain");
            ps.setLong(5, 100);
            ps.setString(6, modifiedAt);
            ps.setString(7, modifiedAt);
            ps.setString(8, java.time.LocalDateTime.now().toString());
            ps.setString(9, "hash-" + fileName + modifiedAt);
            ps.setInt(10, 0);
            ps.setInt(11, 1);
            ps.setInt(12, 2);
            ps.setDouble(13, 0);
            ps.setDouble(14, 0);
            ps.setDouble(15, 0);
            ps.setDouble(16, pathScore);
            ps.setString(17, content);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(FileSql.INSERT_FTS_ROW)) {
            ps.setString(1, fileName);
            ps.setString(2, path);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }
}