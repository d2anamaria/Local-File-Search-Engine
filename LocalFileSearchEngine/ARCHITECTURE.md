# Architecture Overview

This document describes the architecture of the local file search engine using the **C4 model**.
The diagrams move from the overall system view to the internal implementation structure, showing how file crawling, indexing, persistence, and search are organized.

## [Level 1]: System Context
The System Context diagram shows the application in its **environment**, focusing on the user and the external local file system it interacts with.

```mermaid
graph TD
    User["User<br/>[Person]<br/>"]
    App["Local File Search App<br/>[Software System]<br/>A desktop application that provides full-text search on local files."]
    FS["Local File System<br/>[Local System]<br/>Stores files and directories."]


    User -->|Performs searches on local files| App
    App -->|Scans local files from| FS

    classDef person fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;
    classDef system fill:#438dd5,color:#ffffff,stroke:#0b4884,stroke-width:2px;
    classDef local fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;

    class User person;
    class App system;
    class FS local;
```


## [Level 2]: Containers
The Container diagram shows the high-level structure of the system, how responsibilities are distributed across **independently deployable units**.

```mermaid
graph TD
User["User<br/>[Person]<br/>"]

    FS["Local File System<br/>[External System]<br/>Stores files and directories."]

    subgraph AppSystem[" "]
        App["Local File Search Application<br/>[Container: Java]<br/>Provides file indexing and search capabilities over local data."]
        DB["SQLite Database<br/>[Container: SQLite + FTS5]<br/>Stores file metadata and indexed text for search."]
    end

    User -->|Uses| App
    App -->|Scans files from| FS
    App -->|Persists file metadata and indexed text, queries using FTS| DB

    classDef person fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;
    classDef system fill:#438dd5,color:#ffffff,stroke:#0b4884,stroke-width:2px;
    classDef external fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;
    classDef database fill:#438dd5,color:#ffffff,stroke:#0b4884,stroke-width:2px;

    class User person;
    class App system;
    class FS external;
    class DB database;
```

## [Level 3]: Components
The Component diagram decomposes the application into its main responsibilities, separating the indexing flow from the search flow.

```mermaid
graph TD
    %% External
    User["User<br/>[Person]<br/>"]
    FS["Local File System<br/>[Container]"]
    DB["SQLite Database<br/>[Container: SQLite + FTS5]"]

    %% Application boundary
    subgraph App[" "]

        UI["UI<br/>[Component: JavaFX]<br/>Handles user interaction and displays results"]

        %% Search (top-down)
        Search["Search Service<br/>[Component: Java]<br/>Applies search logic"]
        DBAccess["Database Access<br/>[Component: Java]<br/>Access layer to the database"]

        %% Indexing pipeline (top-down)
        Crawler["Crawler<br/>[Component: Java]<br/>Traverses directories, filters files, and returns eligible file paths"]
        Extractor["Content Extractor<br/>[Component: Java]<br/>Extracts text + preview"]
        Indexer["Indexer<br/>[Component: Java]<br/>Processes files and updates indexed content"]

    end

    %% User interaction
    User -->|"Provides input"| UI

    %% Search flow (vertical)
    UI -->|"Forwards search query to"| Search
    Search -->|"Requests matching indexed content from"| DBAccess
   

    %% Indexing flow (vertical, separate lane)
    UI -->|"Triggers indexing (ex: when user starts typing )"|Indexer
    Indexer -->|"Requests file discovery from"| Crawler
    Indexer -->|"Uses to extract text and preview"| Extractor
    Indexer -->|"Requests persistence of indexed data from"| DBAccess

    %% External connections
    Crawler -->|"Reads files from"| FS
    DBAccess -->|"Manages connection, schema, and executes SQL queries on"| DB

    %% Styles
    classDef person fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;
    classDef system fill:#438dd5,color:#ffffff,stroke:#0b4884,stroke-width:2px;
    classDef local fill:#08427b,color:#ffffff,stroke:#052e56,stroke-width:2px;

    class User person;
    class UI,Search,Crawler,Extractor,Indexer,DBAccess system;
    class FS,DB local;
```

## [Level 4]: UML Class Diagrams (before UI)

```mermaid
classDiagram

%% =========================
%% CORE FLOW
%% =========================

App --> Indexer
App --> SearchService
App --> DatabaseManager

Indexer --> RecursiveFileCrawler
Indexer --> TextExtractor
Indexer --> FileIndexRepository

SearchService --> SearchRepository

%% =========================
%% CRAWLER
%% =========================

RecursiveFileCrawler --> IndexingRules
RecursiveFileCrawler --> CrawlResult
CrawlResult --> CrawlStats

%% =========================
%% INDEXING
%% =========================

Indexer --> IndexedFileData
FileIndexRepository --> IndexedFileData

%% =========================
%% SEARCH
%% =========================

SearchRepository --> SearchResult

%% =========================
%% DATABASE
%% =========================

DatabaseManager --> SchemaInitializer
SearchRepository --> SqlQueries
FileIndexRepository --> SqlQueries

%% =========================
%% CLASS DEFINITIONS
%% =========================

class App

class Indexer
class RecursiveFileCrawler
class TextExtractor
class IndexingRules

class FileIndexRepository
class SearchRepository
class DatabaseManager
class SchemaInitializer
class SqlQueries

class IndexedFileData {
path
fileName
extension
mimeType
sizeBytes
preview
content
}

class SearchResult {
fileName
path
preview
}

class CrawlResult
class CrawlStats
```

## Legend

- **Blue elements**: structures that are part of the system being designed
- **Dark blue elements**: external actor or external system
- **Arrows**: main interactions or dependencies between elements