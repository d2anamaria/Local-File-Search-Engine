package searchengine.search;

import searchengine.db.ResultInteractionRepository;

public class ResultInteractionService {

    private final ResultInteractionRepository repository;

    public ResultInteractionService(ResultInteractionRepository repository) {
        this.repository = repository;
    }

    public void recordClick(SearchResult result) {
        if (result != null) {
            repository.recordClick(result.getPath());
        }
    }

    public void recordCopyPath(SearchResult result) {
        if (result != null) {
            repository.recordCopyPath(result.getPath());
        }
    }
}