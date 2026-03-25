package searchengine.db;

import searchengine.indexer.IndexedFileData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FileIndexRepository {

    private final Connection connection;

    public FileIndexRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(IndexedFileData fileData) throws SQLException {
        saveFileMetadata(fileData);
        replaceIndexedContent(fileData);
    }

    private void saveFileMetadata(IndexedFileData fileData) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SqlQueries.INSERT_OR_REPLACE_FILE)) {
            ps.setString(1, fileData.getPath());
            ps.setString(2, fileData.getFileName());
            ps.setString(3, fileData.getExtension());
            ps.setString(4, fileData.getMimeType());
            ps.setLong(5, fileData.getSizeBytes());
            ps.setString(6, fileData.getCreatedAt());
            ps.setString(7, fileData.getModifiedAt());
            ps.setString(8, fileData.getIndexedAt());
            ps.setString(9, fileData.getContentHash());
            ps.setInt(10, fileData.isHidden() ? 1 : 0);
            ps.setInt(11, fileData.isTextFile() ? 1 : 0);
            ps.setString(12, fileData.getPreview());
            ps.executeUpdate();
        }
    }

    private void replaceIndexedContent(IndexedFileData fileData) throws SQLException {
        try (PreparedStatement deletePs = connection.prepareStatement(SqlQueries.DELETE_FTS_BY_PATH)) {
            deletePs.setString(1, fileData.getPath());
            deletePs.executeUpdate();
        }

        if (fileData.getContent() == null || fileData.getContent().isBlank()) {
            return;
        }

        try (PreparedStatement insertPs = connection.prepareStatement(SqlQueries.INSERT_FTS_ROW)) {
            insertPs.setString(1, fileData.getFileName());
            insertPs.setString(2, fileData.getPath());
            insertPs.setString(3, fileData.getContent());
            insertPs.executeUpdate();
        }
    }
}