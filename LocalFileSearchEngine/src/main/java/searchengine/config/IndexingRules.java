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

    public static final Set<String> DEFAULT_IGNORED_DIRECTORIES = Set.of(
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

    public static final Set<String> DEFAULT_IGNORED_FILE_NAMES = Set.of(
            ".DS_Store"
    );

    public static final long DEFAULT_MAX_INDEXED_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private final Set<String> enabledTextExtensions;
    private final Set<String> ignoredDirectories;
    private final Set<String> ignoredFileNames;

    private long maxIndexedFileSizeBytes;
    private boolean includeHiddenFiles;

    public IndexingRules() {
        this.enabledTextExtensions = new LinkedHashSet<>(DEFAULT_TEXT_EXTENSIONS);
        this.ignoredDirectories = new LinkedHashSet<>(DEFAULT_IGNORED_DIRECTORIES);
        this.ignoredFileNames = new LinkedHashSet<>(DEFAULT_IGNORED_FILE_NAMES);
        this.maxIndexedFileSizeBytes = DEFAULT_MAX_INDEXED_FILE_SIZE_BYTES;
        this.includeHiddenFiles = false;
    }

    public Set<String> getEnabledTextExtensions() {
        return Set.copyOf(enabledTextExtensions);
    }

    public void setExtensionEnabled(String extension, boolean enabled) {
        String normalized = normalizeRuleName(extension);

        if (normalized == null) {
            return;
        }

        if (enabled) {
            enabledTextExtensions.add(normalized);
        } else {
            enabledTextExtensions.remove(normalized);
        }
    }

    public boolean isExtensionEnabled(String extension) {
        String normalized = normalizeRuleName(extension);

        if (normalized == null) {
            return false;
        }

        return enabledTextExtensions.contains(normalized);
    }

    public Set<String> getIgnoredDirectories() {
        return Set.copyOf(ignoredDirectories);
    }

    public void addIgnoredDirectory(String directoryName) {
        String normalized = normalizeRuleName(directoryName);

        if (normalized != null) {
            ignoredDirectories.add(normalized);
        }
    }

    public void removeIgnoredDirectory(String directoryName) {
        String normalized = normalizeRuleName(directoryName);

        if (normalized != null) {
            ignoredDirectories.remove(normalized);
        }
    }

    public boolean isIgnoredDirectoryEnabled(String directoryName) {
        String normalized = normalizeRuleName(directoryName);

        if (normalized == null) {
            return false;
        }

        return ignoredDirectories.contains(normalized);
    }

    public Set<String> getIgnoredFileNames() {
        return Set.copyOf(ignoredFileNames);
    }

    public void addIgnoredFileName(String fileName) {
        String normalized = normalizeRuleName(fileName);

        if (normalized != null) {
            ignoredFileNames.add(normalized);
        }
    }

    public void removeIgnoredFileName(String fileName) {
        String normalized = normalizeRuleName(fileName);

        if (normalized != null) {
            ignoredFileNames.remove(normalized);
        }
    }

    public boolean isIgnoredFileNameEnabled(String fileName) {
        String normalized = normalizeRuleName(fileName);

        if (normalized == null) {
            return false;
        }

        return ignoredFileNames.contains(normalized);
    }

    public long getMaxIndexedFileSizeBytes() {
        return maxIndexedFileSizeBytes;
    }

    public void setMaxIndexedFileSizeBytes(long maxIndexedFileSizeBytes) {
        if (maxIndexedFileSizeBytes < 0) {
            return;
        }

        this.maxIndexedFileSizeBytes = maxIndexedFileSizeBytes;
    }

    public boolean isIncludeHiddenFiles() {
        return includeHiddenFiles;
    }

    public void setIncludeHiddenFiles(boolean includeHiddenFiles) {
        this.includeHiddenFiles = includeHiddenFiles;
    }

    public boolean isSupportedTextFile(String fileName) {
        String extension = getExtension(fileName);
        return extension != null && enabledTextExtensions.contains(extension);
    }

    public boolean isIgnoredFolder(String folderName) {
        String normalized = normalizeRuleName(folderName);

        if (normalized == null) {
            return false;
        }

        return ignoredDirectories.contains(normalized);
    }

    public boolean isIgnoredFileName(String fileName) {
        String normalized = normalizeRuleName(fileName);

        if (normalized == null) {
            return false;
        }

        if (ignoredFileNames.contains(normalized)) {
            return true;
        }

        return normalized.endsWith("~")
                || normalized.endsWith(".swp")
                || normalized.endsWith(".tmp");
    }

    public boolean isFileTooLargeForIndexing(long sizeBytes) {
        return sizeBytes > maxIndexedFileSizeBytes;
    }

    public boolean isFileTooLargeForContent(long sizeBytes) {
        return isFileTooLargeForIndexing(sizeBytes);
    }

    public boolean shouldIgnoreDirectory(Path dir) {
        if (dir == null || dir.getFileName() == null) {
            return false;
        }

        String name = dir.getFileName().toString();

        if (!includeHiddenFiles) {
            try {
                if (Files.isHidden(dir)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return isIgnoredFolder(name);
    }

    public boolean shouldIgnoreFile(Path file) {
        if (file == null || file.getFileName() == null) {
            return false;
        }

        String name = file.getFileName().toString();

        if (!includeHiddenFiles) {
            try {
                if (Files.isHidden(file)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
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

    private String normalizeRuleName(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        if (normalized.isBlank()) {
            return null;
        }

        return normalized;
    }
}