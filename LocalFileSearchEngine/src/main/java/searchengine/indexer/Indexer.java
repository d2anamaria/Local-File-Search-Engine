package searchengine.indexer;

import searchengine.extractor.TextExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class Indexer {

    private final TextExtractor extractor = new TextExtractor();

    public void index(Connection connection, List<Path> files) {

        String insertFileSql = """
            INSERT OR REPLACE INTO files(
                path,
                file_name,
                extension,
                mime_type,
                size_bytes,
                created_at,
                modified_at,
                indexed_at,
                content_hash,
                is_hidden,
                is_text_file,
                preview
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String deleteFtsSql = """
            DELETE FROM file_content_fts WHERE path = ?
        """;

        String insertFtsSql = """
            INSERT INTO file_content_fts(
                file_name,
                path,
                content
            )
            VALUES (?, ?, ?)
        """;

        for (Path file : files) {
            try {
                String fileName = file.getFileName().toString();
                String text = extractor.extractText(file);
                String preview = extractor.preview(text);

                String extension = getExtension(fileName);
                String mimeType = Files.probeContentType(file);
                long sizeBytes = Files.size(file);
                String modifiedAt = Files.getLastModifiedTime(file).toInstant().toString();
                String indexedAt = java.time.Instant.now().toString();

                try (PreparedStatement ps = connection.prepareStatement(insertFileSql)) {
                    ps.setString(1, file.toAbsolutePath().toString());
                    ps.setString(2, fileName);
                    ps.setString(3, extension);
                    ps.setString(4, mimeType);
                    ps.setLong(5, sizeBytes);
                    ps.setString(6, null); // created_at
                    ps.setString(7, modifiedAt);
                    ps.setString(8, indexedAt);
                    ps.setString(9, null); // content_hash
                    ps.setInt(10, 0); // is_hidden
                    ps.setInt(11, text.isEmpty() ? 0 : 1);
                    ps.setString(12, preview);

                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(deleteFtsSql)) {
                    ps.setString(1, file.toAbsolutePath().toString());
                    ps.executeUpdate();
                }

                if (!text.isEmpty()) {
                    try (PreparedStatement ps = connection.prepareStatement(insertFtsSql)) {
                        ps.setString(1, fileName);
                        ps.setString(2, file.toAbsolutePath().toString());
                        ps.setString(3, text);
                        ps.executeUpdate();
                    }
                }

                System.out.println("[INDEXED] " + file);

            } catch (Exception e) {
                System.out.println("[ERROR] " + file + " -> " + e.getMessage());
            }
        }
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}