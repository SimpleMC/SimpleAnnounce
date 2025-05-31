# Changelog

## [Unreleased]
### Added
- GH-3: Title type announcement sender (@tajobe)
- MIT license

### Changed
- MC 1.21, Java 21, Kotlin 2.1
- **BREAKING**: New config format

### Fixed
- Config Auto-reload is now a repeating task as intended
- Config actually created on first run
- Copy updated default header when config is updated

### Removed
- Offline variant is now the default jar, no longer producing an "online" version
- debugMode option: log levels are controlled by the server's log4j configuration

## [1.0.0] - 2020-02-10
### Added
- CHANGELOG
- Github Actions for build and automated releases

### Changed
- Adopt semver
- Kotlin rewrite
- Target MC version 1.15

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[Unreleased]: https://github.com/SimpleMC/SimpleAnnounce/compare/release-1.0.0...HEAD
[1.0.0]: https://github.com/SimpleMC/SimpleAnnounce/releases/tag/release-1.0.0
