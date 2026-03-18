# Changelog

## Unreleased

### Documentation

- update README versions to 2.26.0
- update CHANGELOG for 2.26.0

### Miscellaneous

- bump OTel Agent to 2.26.0, SDK to 1.60.1
## 2.25.0 — 2026-03-03

### Bug Fixes

- use JDK 21 to run Gradle 9.x (compile target remains Java 11)

### Documentation

- add bilingual README and rename project to opentelemetry-javaagent-extensions

### Features

- add OTel Java Agent extension for TraceId/SpanId response headers

### Miscellaneous

- add git-cliff for CHANGELOG generation and release notes
- use semver tag pattern and remove v prefix from URLs

### Refactor

- migrate to Gradle version catalog and bump to OTel 2.25.0

