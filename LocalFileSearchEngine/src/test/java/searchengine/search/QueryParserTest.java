package searchengine.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

    private final QueryParser parser = new QueryParser();

    @Test
    void parsesContentOnlyQuery() {
        SearchQuery query = parser.parse("hello world");

        assertEquals(1, query.getContent().size());
        assertEquals("hello world", query.getContent().get(0));
        assertTrue(query.getPath().isEmpty());
    }

    // Verifies that both path: and content: qualifiers are correctly extracted
    // and stored in their respective fields.
    @Test
    void parsesPathAndContentQualifiers() {
        SearchQuery query = parser.parse("path:src content:hello");

        assertEquals(1, query.getPath().size());
        assertEquals("src", query.getPath().get(0));

        assertEquals(1, query.getContent().size());
        assertEquals("hello", query.getContent().get(0));
    }

    // Verifies that the parser correctly handles qualifiers regardless of order
    // (order should not affect parsing result).
    @Test
    void parsesQualifiersInAnyOrder() {
        SearchQuery query = parser.parse("content:hello path:src");

        assertEquals("hello", query.getContent().get(0));
        assertEquals("src", query.getPath().get(0));
    }

    // Verifies that multiple occurrences of the same qualifier are combined
    // (AND semantics), accumulating all values.
    @Test
    void combinesDuplicateQualifiers() {
        SearchQuery query = parser.parse("path:src path:main content:hello content:world");

        assertEquals(2, query.getPath().size());
        assertTrue(query.getPath().contains("src"));
        assertTrue(query.getPath().contains("main"));

        assertEquals(2, query.getContent().size());
        assertTrue(query.getContent().contains("hello"));
        assertTrue(query.getContent().contains("world"));
    }

    @Test
    void ignoresEmptyQualifiers() {
        SearchQuery query = parser.parse("path: content:");

        assertTrue(query.isEmpty());
    }

    @Test
    void emptyInputProducesEmptyQuery() {
        SearchQuery query = parser.parse("   ");

        assertTrue(query.isEmpty());
    }
}