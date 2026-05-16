package searchengine.extractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ImageColorExtractor {

    private static final double GRAYSCALE_THRESHOLD = 0.8;

    public String extractDominantColor(Path path) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());

            if (image == null) {
                return null;
            }

            int count = 0;
            int countGrayscale = 0;
            Map<String, Integer> colorCounts = new HashMap<>();

            int stepX = Math.max(1, image.getWidth() / 100);
            int stepY = Math.max(1, image.getHeight() / 100);

            for (int y = 0; y < image.getHeight(); y += stepY) {
                for (int x = 0; x < image.getWidth(); x += stepX) {
                    Color color = new Color(image.getRGB(x, y), true);

                    if (color.getAlpha() < 128) {
                        continue;
                    }

                    if (isGrayscale(color)) {
                        countGrayscale++;
                    }

                    String colorName = classifyColor(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue()
                    );

                    colorCounts.merge(colorName, 1, Integer::sum);
                    count++;
                }
            }

            if (count == 0) {
                return null;
            }

            if ((double) countGrayscale / count > GRAYSCALE_THRESHOLD) {
                return "grayscale";
            }

            return mostFrequentColor(colorCounts);
        } catch (Exception e) {
            return null;
        }
    }

    private String mostFrequentColor(Map<String, Integer> colorCounts) {
        return colorCounts.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("mixed"))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("mixed");
    }

    private String classifyColor(int r, int g, int b) {
        if (r < 50 && g < 50 && b < 50) return "black";
        if (r > 210 && g > 210 && b > 210) return "white";

        if (r > g * 1.3 && r > b * 1.3) return "red";
        if (g > r * 1.3 && g > b * 1.3) return "green";
        if (b > r * 1.3 && b > g * 1.3) return "blue";

        if (r > 180 && g > 140 && b < 100) return "yellow";
        if (r > 150 && b > 120 && g < 120) return "purple";
        if (r > 180 && g > 90 && g < 170 && b < 100) return "orange";

        return "mixed";
    }

    private boolean isGrayscale(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        return Math.abs(r - g) < 15
                && Math.abs(g - b) < 15
                && Math.abs(r - b) < 15;
    }
}