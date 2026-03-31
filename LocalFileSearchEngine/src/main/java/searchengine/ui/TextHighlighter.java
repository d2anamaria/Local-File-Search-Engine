package searchengine.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public final class TextHighlighter {

    private TextHighlighter() {
    }

    public static TextFlow buildHighlightedTextFlow(String fullText, String query) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(2);
        flow.setStyle("-fx-padding: 10; -fx-background-color: white;");

        if (fullText == null || fullText.isEmpty()) {
            flow.getChildren().add(createNormalText("No text available."));
            return flow;
        }

        if (query == null || query.isBlank()) {
            flow.getChildren().add(createNormalText(fullText));
            return flow;
        }

        List<String> terms = extractHighlightTerms(query);

        if (terms.isEmpty()) {
            flow.getChildren().add(createNormalText(fullText));
            return flow;
        }

        int index = 0;
        String lowerText = fullText.toLowerCase();

        while (index < fullText.length()) {
            int earliestMatch = -1;
            String matchedTerm = null;

            for (String term : terms) {
                int found = lowerText.indexOf(term.toLowerCase(), index);

                if (found != -1 && (earliestMatch == -1 || found < earliestMatch)) {
                    earliestMatch = found;
                    matchedTerm = term;
                }
            }

            if (earliestMatch == -1) {
                flow.getChildren().add(createNormalText(fullText.substring(index)));
                break;
            }

            if (earliestMatch > index) {
                flow.getChildren().add(createNormalText(fullText.substring(index, earliestMatch)));
            }

            flow.getChildren().add(createHighlightedText(
                    fullText.substring(earliestMatch, earliestMatch + matchedTerm.length())
            ));

            index = earliestMatch + matchedTerm.length();
        }

        return flow;
    }

    private static List<String> extractHighlightTerms(String query) {
        String cleaned = query.replace("\"", " ").trim();

        if (cleaned.isBlank()) {
            return List.of();
        }

        String[] parts = cleaned.split("\\s+");
        List<String> terms = new ArrayList<>();

        for (String part : parts) {
            if (!part.isBlank()) {
                terms.add(part);
            }
        }

        terms.sort((a, b) -> Integer.compare(b.length(), a.length()));
        return terms;
    }

    private static Text createNormalText(String value) {
        Text text = new Text(value);
        text.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        return text;
    }

    private static Text createHighlightedText(String value) {
        Text text = new Text(value);
        text.setFill(Color.BLACK);
        text.setStyle("""
                -fx-font-family: 'Monospaced';
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-underline: true;
                """);
        return text;
    }
}