package searchengine.extractor;

import searchengine.config.IndexingRules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextExtractor {

    private static final int PREVIEW_LINE_COUNT = 3;

    private final IndexingRules indexingRules;

    public TextExtractor(IndexingRules indexingRules) {
        this.indexingRules = indexingRules;
    }

    public String extractText(Path path) {
        try {
            String fileName = path.getFileName().toString();

            if (!indexingRules.isSupportedTextFile(fileName)) {
                return "";
            }

            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String[] lines = text.split("\\R");
        StringBuilder result = new StringBuilder();
        int addedLines = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            if (addedLines > 0) {
                result.append("\n");
            }

            result.append(trimmed);
            addedLines++;

            if (addedLines == PREVIEW_LINE_COUNT) {
                break;
            }
        }

        return result.toString();
    }
}