package searchengine.config;

import java.util.Set;

public class IndexingRules {

    // --- Supported text file extensions (for content indexing)
    public static final Set<String> SUPPORTED_TEXT_EXTENSIONS = Set.of(
            "txt", "md",
            "java", "kt", "c", "cpp", "h", "hpp",
            "py",
            "js", "ts",
            "html", "css", "xml",
            "json",
            "yml", "yaml",
            "sql",
            "csv",
            "log"
    );

    // --- Folders to skip entirely (do not traverse)
    public static final Set<String> IGNORED_FOLDERS = Set.of(
            ".git",
            ".idea",
            ".vscode",
            ".gradle",
            "build",
            "target",
            "out",
            "node_modules",
            "bin",
            "obj",
            "__pycache__"
    );

    // --- Specific file names to ignore
    public static final Set<String> IGNORED_FILE_NAMES = Set.of(
            ".DS_Store"
    );

    // --- Max file size for content indexing (10 MB)
    public static final long MAX_CONTENT_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    // -----------------------------
    // Utility methods
    // -----------------------------

    public static boolean isSupportedTextFile(String fileName) {
        if (fileName == null) return false;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return false;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return SUPPORTED_TEXT_EXTENSIONS.contains(extension);
    }

    public static boolean isIgnoredFolder(String folderName) {
        if (folderName == null) return false;
        return IGNORED_FOLDERS.contains(folderName);
    }

    public static boolean isIgnoredFileName(String fileName) {
        if (fileName == null) return false;

        if (IGNORED_FILE_NAMES.contains(fileName)) {
            return true;
        }

        return fileName.endsWith("~")
                || fileName.endsWith(".swp")
                || fileName.endsWith(".tmp");
    }

    public static boolean isFileTooLargeForContent(long sizeBytes) {
        return sizeBytes > MAX_CONTENT_FILE_SIZE_BYTES;
    }
}