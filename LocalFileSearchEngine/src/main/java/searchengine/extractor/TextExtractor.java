package searchengine.extractor;

import searchengine.config.IndexingRules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextExtractor {

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
        String result = "";

        for (int i = 0; i < lines.length && i < 3; i++) {
            result += lines[i] + "\n";
        }

        return result.trim();
    }
}