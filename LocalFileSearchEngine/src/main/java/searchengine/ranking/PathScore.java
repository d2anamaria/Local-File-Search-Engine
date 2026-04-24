package searchengine.ranking;

import java.nio.file.Path;

public class PathScore {
    private final int pathDepth;
    private final double directoryScore;
    private final double extensionScore;
    private final double sizeScore;
    private final double pathScore;

    public PathScore(Path rootPath, Path file, String extension, long sizeBytes) {
        Path relativePath = rootPath.toAbsolutePath().relativize(file.toAbsolutePath());

        this.pathDepth = relativePath.getNameCount();
        this.directoryScore = computeDirectoryScore(relativePath);
        this.extensionScore = computeExtensionScore(extension);
        this.sizeScore = computeSizeScore(sizeBytes);

        this.pathScore = Math.max(
                directoryScore + extensionScore + sizeScore - pathDepth * 2.0,
                0.0
        );
    }

    public int getPathDepth() {
        return pathDepth;
    }

    public double getDirectoryScore() {
        return directoryScore;
    }

    public double getExtensionScore() {
        return extensionScore;
    }

    public double getSizeScore() {
        return sizeScore;
    }

    public double getPathScore() {
        return pathScore;
    }

    private double computeDirectoryScore(Path relativePath) {
        String normalizedPath = relativePath.toString().toLowerCase();

        double score = 0.0;

        if (containsDirectory(normalizedPath, "src")) {
            score += 20.0;
        }

        if (containsDirectory(normalizedPath, "docs") || containsDirectory(normalizedPath, "doc")) {
            score += 15.0;
        }

        if (containsDirectory(normalizedPath, "test") || containsDirectory(normalizedPath, "tests")) {
            score += 8.0;
        }

        if (containsDirectory(normalizedPath, "build")
                || containsDirectory(normalizedPath, "target")
                || containsDirectory(normalizedPath, "out")) {
            score -= 15.0;
        }

        return score;
    }

    private boolean containsDirectory(String path, String directoryName) {
        return path.equals(directoryName)
                || path.startsWith(directoryName + "/")
                || path.startsWith(directoryName + "\\")
                || path.contains("/" + directoryName + "/")
                || path.contains("\\" + directoryName + "\\");
    }

    private double computeExtensionScore(String extension) {
        return switch (extension) {
            case "java", "kt", "cpp", "c", "h", "hpp", "py", "js", "ts" -> 15.0;
            case "md", "txt", "sql", "json", "xml", "yaml", "yml" -> 10.0;
            case "html", "css", "csv", "log" -> 5.0;
            case "class", "exe", "dll", "bin", "jar" -> -20.0;
            default -> 0.0;
        };
    }

    private double computeSizeScore(long sizeBytes) {
        if (sizeBytes < 50_000) {
            return 10.0;
        }

        if (sizeBytes < 500_000) {
            return 5.0;
        }

        if (sizeBytes > 5_000_000) {
            return -10.0;
        }

        return 0.0;
    }
}