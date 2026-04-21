package searchengine.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern QUALIFIER =
            Pattern.compile("(content|path):", Pattern.CASE_INSENSITIVE);

    public SearchQuery parse(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new SearchQuery("", "");
        }

        List<String> contentParts = new ArrayList<>();
        List<String> pathParts = new ArrayList<>();

        Matcher matcher = QUALIFIER.matcher(rawQuery);

        int last = 0;
        String currentQualifier = null;

        while (matcher.find()) {
            String chunk = rawQuery.substring(last, matcher.start()).trim();

            if (currentQualifier == null) {
                if (!chunk.isBlank()) {
                    contentParts.add(chunk); // default = content
                }
            } else if (currentQualifier.equals("content")) {
                if (!chunk.isBlank()) {
                    contentParts.add(chunk);
                }
            } else if (currentQualifier.equals("path")) {
                if (!chunk.isBlank()) {
                    pathParts.add(chunk);
                }
            }

            currentQualifier = matcher.group(1).toLowerCase();
            last = matcher.end();
        }

        String lastChunk = rawQuery.substring(last).trim();

        if (currentQualifier == null) {
            if (!lastChunk.isBlank()) {
                contentParts.add(lastChunk);
            }
        } else if (currentQualifier.equals("content")) {
            if (!lastChunk.isBlank()) {
                contentParts.add(lastChunk);
            }
        } else if (currentQualifier.equals("path")) {
            if (!lastChunk.isBlank()) {
                pathParts.add(lastChunk);
            }
        }

        return new SearchQuery(
                normalize(String.join(" ", contentParts)),
                normalize(String.join(" ", pathParts))
        );
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }
}