package searchengine.db.searchquery;

import searchengine.config.IndexingRules;
import searchengine.search.SearchQuery;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchParameterBinder {

    public void bind(
            PreparedStatement ps,
            SearchSqlBuilder.SearchSqlContext context,
            SearchQuery query,
            String rootPath,
            IndexingRules rules
    ) throws SQLException {
        int parameterIndex = 1;

        for (String term : context.terms()) {
            ps.setString(parameterIndex++, term.toLowerCase());
        }

        if (context.hasContent()) {
            ps.setString(parameterIndex++, toFtsAndQuery(query.getContent()));

            if (context.underRoot()) {
                ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));
            }

            for (String pathPart : query.getPath()) {
                ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
            }
        } else {
            if (context.underRoot()) {
                ps.setString(parameterIndex++, normalizeRootPrefix(rootPath));
            }

            for (String pathPart : query.getPath()) {
                ps.setString(parameterIndex++, "%" + pathPart.toLowerCase() + "%");
            }
        }

        if (!rules.isIncludeHiddenFiles()) {
            ps.setInt(parameterIndex++, 0);
        }

        ps.setLong(parameterIndex++, rules.getMaxIndexedFileSizeBytes());

        for (String extension : rules.getEnabledTextExtensions()) {
            ps.setString(parameterIndex++, extension);
        }
    }

    private String toFtsAndQuery(List<String> contentParts) {
        List<String> terms = new ArrayList<>();

        for (String part : contentParts) {
            for (String word : part.trim().split("\\s+")) {
                if (!word.isBlank()) {
                    terms.add(word + "*");
                }
            }
        }

        return String.join(" AND ", terms);
    }

    private String normalizeRootPrefix(String rootPath) {
        if (rootPath == null || rootPath.isBlank()) {
            return "%";
        }

        String normalized = rootPath.replace("\\", "/");

        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized + "%";
    }
}