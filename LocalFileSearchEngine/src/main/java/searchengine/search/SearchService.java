package searchengine.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SearchService {

    public void search(Connection connection, String query) {
        String sql = """
            SELECT f.file_name, f.path, f.preview
            FROM file_content_fts fts
            JOIN files f ON f.path = fts.path
            WHERE file_content_fts MATCH ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, query);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("File: " + rs.getString("file_name"));
                    System.out.println("Path: " + rs.getString("path"));
                    System.out.println("Preview:");
                    System.out.println(rs.getString("preview"));
                    System.out.println("-----");
                }
            }

        } catch (Exception e) {
            System.out.println("[SEARCH ERROR] " + e.getMessage());
        }
    }
}