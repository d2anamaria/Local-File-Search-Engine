package searchengine.db.searchquery.filter;

import java.util.List;

public final class QueryFilterRegistry {

    private QueryFilterRegistry() {}

    public static List<QueryFilter> filters() {
        return List.of(
                new ContentQueryFilter(),
                new RootPathQueryFilter(),
                new PathQueryFilter(),
                new HiddenFileQueryFilter(),
                new SizeQueryFilter(),
                new ExtensionQueryFilter(),
                new ColorQueryFilter()
        );
    }
}