package searchengine.crawler;

public class CrawlStats {
    private int directoriesVisited;
    private int filesDiscovered;
    private int filesSkipped;
    private int errors;

    public void incrementDirectoriesVisited() {
        directoriesVisited++;
    }

    public void incrementFilesDiscovered() {
        filesDiscovered++;
    }

    public void incrementFilesSkipped() {
        filesSkipped++;
    }

    public void incrementErrors() {
        errors++;
    }

    public int getDirectoriesVisited() {
        return directoriesVisited;
    }

    public int getFilesDiscovered() {
        return filesDiscovered;
    }

    public int getFilesSkipped() {
        return filesSkipped;
    }

    public int getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "CrawlStats{" +
                "directoriesVisited=" + directoriesVisited +
                ", filesDiscovered=" + filesDiscovered +
                ", filesSkipped=" + filesSkipped +
                ", errors=" + errors +
                '}';
    }
}