package searchengine.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class IndexingRules {

    public static final Set<String> DEFAULT_TEXT_EXTENSIONS = Set.of(
            "txt", "md",
            "java", "kt", "c", "cpp", "h", "hpp", "rs",
            "py",
            "js", "ts",
            "html", "css", "xml",
            "json",
            "yml", "yaml",
            "sql",
            "csv",
            "log"
    );

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

    public static final Set<String> IGNORED_FILE_NAMES = Set.of(
            ".DS_Store"
    );

    public static final long MAX_CONTENT_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private final Set<String> enabledTextExtensions = new LinkedHashSet<>(DEFAULT_TEXT_EXTENSIONS);

    public Set<String> getEnabledTextExtensions() {
        return Set.copyOf(enabledTextExtensions);
    }

    public void setExtensionEnabled(String extension, boolean enabled) {
        if (extension == null || extension.isBlank()) {
            return;
        }

        String normalized = extension.toLowerCase();

        if (enabled) {
            enabledTextExtensions.add(normalized);
        } else {
            enabledTextExtensions.remove(normalized);
        }
    }

    public boolean isExtensionEnabled(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }

        return enabledTextExtensions.contains(extension.toLowerCase());
    }

    public boolean isSupportedTextFile(String fileName) {
        String extension = getExtension(fileName);
        return extension != null && enabledTextExtensions.contains(extension);
    }

    public boolean isIgnoredFolder(String folderName) {
        if (folderName == null) return false;
        return IGNORED_FOLDERS.contains(folderName);
    }

    public boolean isIgnoredFileName(String fileName) {
        if (fileName == null) return false;

        if (IGNORED_FILE_NAMES.contains(fileName)) {
            return true;
        }

        return fileName.endsWith("~")
                || fileName.endsWith(".swp")
                || fileName.endsWith(".tmp");
    }

    public boolean isFileTooLargeForContent(long sizeBytes) {
        return sizeBytes > MAX_CONTENT_FILE_SIZE_BYTES;
    }

    public boolean shouldIgnoreDirectory(Path dir) {
        if (dir == null || dir.getFileName() == null) {
            return false;
        }

        String name = dir.getFileName().toString();

        try {
            if (Files.isHidden(dir)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        return isIgnoredFolder(name);
    }

    public boolean shouldIgnoreFile(Path file) {
        if (file == null || file.getFileName() == null) {
            return false;
        }

        String name = file.getFileName().toString();

        try {
            if (Files.isHidden(file)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        return isIgnoredFileName(name);
    }

    public boolean isSupportedFileType(Path file) {
        if (file == null || file.getFileName() == null) {
            return false;
        }

        return isSupportedTextFile(file.getFileName().toString());
    }

    public String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(lastDot + 1).toLowerCase();
    }
}