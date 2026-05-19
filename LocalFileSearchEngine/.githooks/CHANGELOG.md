# Changelog

All notable changes to this project will be documented in this file.

# Iteration 3 — Multimodal Indexing & Media Support
## [3.1.0] - 2026-05-19

### Added
- Added modular `QueryFilter` architecture for SQL query generation ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Added `QueryFilterRegistry` for centralized filter registration ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Added isolated query filters for:
  - content filtering
  - path filtering
  - root path filtering
  - hidden file filtering
  - extension filtering
  - file size filtering ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Added integration tests for content and path-qualified search behavior ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Added `IndexingStrategyRegistry` for centralized indexing strategy configuration ([ff9b7dd](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ff9b7dd))

### Changed
- Refactored `SearchSqlBuilder` from combinational query branching to dynamic filter orchestration ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Refactored `SearchParameterBinder` to delegate parameter binding to query filters ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Converted `SearchSql` into a reusable SQL fragment provider ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))
- Simplified SQL generation flow by removing duplicated query combinations ([ea59887](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ea59887))

### Infrastructure
- Improved extensibility for future query qualifiers and multimodal search support.
- Reduced coupling between query parsing, SQL generation, and parameter binding.
- Improved Open/Closed Principle compliance for search query extensions.

## [3.0.0] - 2026-05-16

### Added
- Added strategy-based file indexing architecture ([a172c8d](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a172c8d))
- Added image extension support to indexing rules ([9f04597](https://github.com/d2anamaria/Local-File-Search-Engine/commit/9f04597))
- Added dominant color extraction for indexed images ([3c2f9e9](https://github.com/d2anamaria/Local-File-Search-Engine/commit/3c2f9e9))
- Added multimodal indexing metadata fields:
    - `file_category`
    - `dominant_color` ([a4386b5](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a4386b5))
- Added image metadata persistence support in:
    - schema initialization
    - repositories
    - SQL insert/update pipelines ([a4386b5](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a4386b5))

### Changed
- Refactored the `Indexer` to delegate processing through indexing strategies ([67efb69](https://github.com/d2anamaria/Local-File-Search-Engine/commit/67efb69))
- Simplified and reorganized text extraction behavior ([3c2f9e9](https://github.com/d2anamaria/Local-File-Search-Engine/commit/3c2f9e9))
- Extended DB schema to support multimodal metadata storage ([a4386b5](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a4386b5))
- Updated indexed file metadata model and persistence flow for media-aware indexing ([a4386b5](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a4386b5))

### Infrastructure
- Improved indexing extensibility through strategy-based processing.
- Prepared indexing pipeline for future media-specific extraction workflows.

# Iteration 2 — Ranking & Query Intelligence

## [2.6.0] - 2026-04-29

### Changed
- Stabilized JavaFX search result cell layout and rendering ([c65dfde](https://github.com/d2anamaria/Local-File-Search-Engine/commit/c65dfde))
- Improved ranking consistency across strategy switches ([c65dfde](https://github.com/d2anamaria/Local-File-Search-Engine/commit/c65dfde))
- Reorganized DB/search architecture by separating:
    - SQL generation
    - parameter binding
    - result mapping
    - file rule matching ([6ee62c3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/6ee62c3), [7f0fd24](https://github.com/d2anamaria/Local-File-Search-Engine/commit/7f0fd24))

---

## [2.5.0] - 2026-04-28

### Added
- Added unit and integration tests for:
    - query parser behavior
    - ranking strategies
    - user relevance scoring
    - interaction tracking
    - search history persistence ([1f1baeb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/1f1baeb))
- Added SQL-based validation for:
    - recency scoring
    - extension preference boosts
    - click/copy-path interaction weighting
    - term-file interaction scoring ([1f1baeb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/1f1baeb))

### Changed
- Improved ranking strategy extensibility through registry decoupling ([66518b7](https://github.com/d2anamaria/Local-File-Search-Engine/commit/66518b7))
- Improved search responsiveness through DB/search layer reorganization ([6ee62c3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/6ee62c3), [7f0fd24](https://github.com/d2anamaria/Local-File-Search-Engine/commit/7f0fd24))
- Refined autocomplete behavior and suggestion handling ([5df99cb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/5df99cb), [0da38df](https://github.com/d2anamaria/Local-File-Search-Engine/commit/0da38df))

### Fixed
- Fixed unwanted autocomplete suggestion selection while typing ([0da38df](https://github.com/d2anamaria/Local-File-Search-Engine/commit/0da38df))
- Fixed suggestion ordering edge cases ([5df99cb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/5df99cb))

---

## [2.4.0] - 2026-04-28

### Added
- Added behavioral user relevance scoring using:
    - result click frequency
    - copy-path frequency
    - file recency
    - extension interaction statistics
    - term-file interaction history ([aad102b](https://github.com/d2anamaria/Local-File-Search-Engine/commit/aad102b), [a302c43](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a302c43))
- Added interaction tracking for:
    - result clicks
    - copy path actions
    - open folder actions ([42827b1](https://github.com/d2anamaria/Local-File-Search-Engine/commit/42827b1))
- Added cleanup support for stale interaction data ([67dcbc9](https://github.com/d2anamaria/Local-File-Search-Engine/commit/67dcbc9))
- Added term-file interaction scoring and aggregation ([a302c43](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a302c43))

### Changed
- Improved result ordering using weighted behavioral signals ([fe50bff](https://github.com/d2anamaria/Local-File-Search-Engine/commit/fe50bff))
- Refined ranking score composition and interaction-based SQL aggregation ([aad102b](https://github.com/d2anamaria/Local-File-Search-Engine/commit/aad102b), [a302c43](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a302c43))

---

## [2.3.0] - 2026-04-27

### Added
- Added observer-based search history tracking ([def1ed3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/def1ed3))
- Added autocomplete suggestions backed by search history ([def1ed3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/def1ed3))
- Added debounced query logging to capture final user intent ([ce5d42f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce5d42f))
- Added support for multiple `path:` qualifiers combined with AND semantics ([7a6433f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/7a6433f))

### Changed
- Improved search suggestion update flow and UI responsiveness ([ce5d42f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce5d42f), [5df99cb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/5df99cb))

### Fixed
- Fixed accidental autocomplete selection while typing spaces ([0da38df](https://github.com/d2anamaria/Local-File-Search-Engine/commit/0da38df))
- Ensured typed query always remains the first suggestion ([5df99cb](https://github.com/d2anamaria/Local-File-Search-Engine/commit/5df99cb))

---

## [2.2.0] - 2026-04-26

### Added
- Added swappable ranking strategies:
    - PathScoreStrategy
    - ModifiedDateStrategy
    - AlphabeticalStrategy
    - UserRelevanceStrategy ([85f2fc8](https://github.com/d2anamaria/Local-File-Search-Engine/commit/85f2fc8))
- Added centralized ranking strategy registry ([66518b7](https://github.com/d2anamaria/Local-File-Search-Engine/commit/66518b7))
- Added runtime ranking strategy switching from the UI ([85f2fc8](https://github.com/d2anamaria/Local-File-Search-Engine/commit/85f2fc8))

### Changed
- Decoupled ranking strategy selection from the search controller ([66518b7](https://github.com/d2anamaria/Local-File-Search-Engine/commit/66518b7))
- Improved JavaFX search toolbar layout and compact controls ([85f2fc8](https://github.com/d2anamaria/Local-File-Search-Engine/commit/85f2fc8))

---

## [2.1.0] - 2026-04-24

### Added
- Added index-time path scoring system ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))
- Added directory importance heuristics and path metadata scoring ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))
- Added extension-based and size-based ranking signals ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))
- Added persisted path score metadata for ranking ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))

### Changed
- Improved search result ordering using precomputed path relevance ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))
- Optimized ranking performance by computing scores during indexing ([ce14d2f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/ce14d2f))

---

## [2.0.0] - 2026-04-21

### Added
- Added query parser supporting:
    - `content:`
    - `path:`
      qualifiers ([2002f98](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2002f98))
- Added support for arbitrary qualifier ordering ([2002f98](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2002f98))
- Added support for multiple `path:` qualifiers with AND semantics ([7a6433f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/7a6433f))

### Changed
- Improved query interpretation and structured filtering behavior ([2002f98](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2002f98))

---
# Iteration 1 — Search Infrastructure Foundations

## [1.5.0] - 2026-03-31

### Added
- Added safe-stop indexing support with partial indexing results ([b4f3844](https://github.com/d2anamaria/Local-File-Search-Engine/commit/b4f3844))
- Added indexing timing statistics and processing duration tracking ([0c4783c](https://github.com/d2anamaria/Local-File-Search-Engine/commit/0c4783c))
- Added chunked database transaction buffering for indexing operations ([2ede9fc](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2ede9fc))
- Added indexing progress tracking and indexing reports ([55a05af](https://github.com/d2anamaria/Local-File-Search-Engine/commit/55a05af))
- Added runtime-configurable indexing and search rules ([3b39f59](https://github.com/d2anamaria/Local-File-Search-Engine/commit/3b39f59))
- Added result details dialog and optimized preview generation ([bdf997c](https://github.com/d2anamaria/Local-File-Search-Engine/commit/bdf997c))

### Changed
- Improved indexing responsiveness and DB concurrency handling ([a1459a9](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a1459a9))
- Reorganized UI into controller, component, and utility packages ([7c280c3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/7c280c3), [98b839b](https://github.com/d2anamaria/Local-File-Search-Engine/commit/98b839b))
- Split monolithic controller and centralized UI composition logic ([2bb3d7f](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2bb3d7f))
- Refactored DB responsibilities and moved SQL logic into dedicated layers ([877ea17](https://github.com/d2anamaria/Local-File-Search-Engine/commit/877ea17), [f211f0a](https://github.com/d2anamaria/Local-File-Search-Engine/commit/f211f0a))

---

## [1.3.0] - 2026-03-27

### Added
- Added incremental reindexing:
    - skip unchanged files
    - update modified files
    - remove deleted files ([d1cf7b4](https://github.com/d2anamaria/Local-File-Search-Engine/commit/d1cf7b4))
- Added directory-scoped search support using indexed path filtering ([423eb2e](https://github.com/d2anamaria/Local-File-Search-Engine/commit/423eb2e))

---

## [1.2.0] - 2026-03-26

### Added
- Added JavaFX-based search interface ([e7d643a](https://github.com/d2anamaria/Local-File-Search-Engine/commit/e7d643a))
- Added folder picker with automatic indexing trigger ([4e5acd9](https://github.com/d2anamaria/Local-File-Search-Engine/commit/4e5acd9))

---

## [1.1.0] - 2026-03-25

### Added
- Added C4 architecture diagrams ([fd4efbf](https://github.com/d2anamaria/Local-File-Search-Engine/commit/fd4efbf))
- Added high-level architecture documentation ([57ecae3](https://github.com/d2anamaria/Local-File-Search-Engine/commit/57ecae3))

### Changed
- Refactored responsibilities between:
    - UI
    - DB
    - indexing
    - search layers ([877ea17](https://github.com/d2anamaria/Local-File-Search-Engine/commit/877ea17), [f211f0a](https://github.com/d2anamaria/Local-File-Search-Engine/commit/f211f0a))

---

## [1.0.0] - 2026-03-23

### Added
- Added primitive FTS-based search component ([a464af7](https://github.com/d2anamaria/Local-File-Search-Engine/commit/a464af7))
- Added SQLite database integration and initial schema ([0f4869e](https://github.com/d2anamaria/Local-File-Search-Engine/commit/0f4869e), [f5086ff](https://github.com/d2anamaria/Local-File-Search-Engine/commit/f5086ff))
- Added text extraction and preview generation ([2217ffd](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2217ffd))
- Added file indexing pipeline and FTS5 population ([07e8a31](https://github.com/d2anamaria/Local-File-Search-Engine/commit/07e8a31))

---

## [0.1.0] - 2026-03-22

### Added
- Created initial project structure ([5defac0](https://github.com/d2anamaria/Local-File-Search-Engine/commit/5defac0))
- Added recursive file crawler with skip rules ([2fb3bdd](https://github.com/d2anamaria/Local-File-Search-Engine/commit/2fb3bdd))
- Added indexing rules:
    - supported file types
    - ignored folders
    - size limits ([1ad6cac](https://github.com/d2anamaria/Local-File-Search-Engine/commit/1ad6cac), [9c95a4a](https://github.com/d2anamaria/Local-File-Search-Engine/commit/9c95a4a))

---