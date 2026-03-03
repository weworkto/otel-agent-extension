# OpenTelemetry Java Agent Extension

[![Build & Release](https://github.com/weworkto/opentelemetry-javaagent-extensions/actions/workflows/release.yml/badge.svg)](https://github.com/weworkto/opentelemetry-javaagent-extensions/actions/workflows/release.yml)
[![GitHub Release](https://img.shields.io/github/v/release/weworkto/opentelemetry-javaagent-extensions?label=release)](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/latest)
[![OTel Agent](https://img.shields.io/badge/OTel_Java_Agent-2.25.0-blueviolet)](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/tag/v2.25.0)
[![Java](https://img.shields.io/badge/Java-11%2B-orange)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3.1-02303A?logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/github/license/weworkto/opentelemetry-javaagent-extensions)](LICENSE)

**English** | **[中文](README.md)**

A lightweight [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) extension that **automatically injects TraceId and SpanId into HTTP response headers**, enabling frontend-to-backend trace correlation without any code changes.

---

## How It Works

This extension implements the [`HttpServerResponseCustomizer`](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/javaagent-extension-api/src/main/java/io/opentelemetry/javaagent/bootstrap/http/HttpServerResponseCustomizer.java) SPI provided by the OTel Java Agent. It hooks into every HTTP server response and appends the current trace context as response headers:

```
HTTP/1.1 200 OK
Content-Type: application/json
TraceId: 0af7651916cd43dd8448eb211c80319c
SpanId: b7ad6b7169203331
```

Once deployed, you can:

- Copy the `TraceId` from any HTTP response to search in your tracing backend (Jaeger, Tempo, etc.)
- Build frontend error reporting that automatically attaches trace IDs
- Correlate client-side issues with server-side distributed traces

## Quick Start

### 1. Download

Grab the latest JAR from [Releases](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/latest):

```bash
curl -fsSL -o opentelemetry-javaagent-extensions.jar \
  "https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/download/v2.25.0/opentelemetry-javaagent-extensions-2.25.0.jar"
```

### 2. Run with your application

```bash
java -javaagent:/path/to/opentelemetry-javaagent.jar \
     -Dotel.javaagent.extensions=/path/to/opentelemetry-javaagent-extensions.jar \
     -jar your-app.jar
```

### 3. Verify

```bash
curl -si http://localhost:8080/api/health | grep -E "TraceId|SpanId"
# TraceId: 0af7651916cd43dd8448eb211c80319c
# SpanId: b7ad6b7169203331
```

## Docker Integration

In a Dockerfile, download the extension alongside the OTel Java Agent:

```dockerfile
ARG OTEL_AGENT_VERSION=2.25.0
ARG OTEL_EXT_VERSION=2.25.0

RUN curl -fsSL -o /agent/opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar"

RUN curl -fsSL -o /agent/opentelemetry-javaagent-extensions.jar \
    "https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/download/v${OTEL_EXT_VERSION}/opentelemetry-javaagent-extensions-${OTEL_EXT_VERSION}.jar"
```

## Kubernetes Deployment

```yaml
env:
  - name: JAVA_TOOL_OPTIONS
    value: >-
      -javaagent:/agent/opentelemetry-javaagent.jar
      -Dotel.javaagent.extensions=/agent/opentelemetry-javaagent-extensions.jar
      -Dotel.service.name=my-service
      -Dotel.exporter.otlp.endpoint=http://otel-collector:4317
```

## Response Headers

| Header    | Description                              | Example                            |
|-----------|------------------------------------------|------------------------------------|
| `TraceId` | W3C Trace ID, 32 hex characters          | `0af7651916cd43dd8448eb211c80319c` |
| `SpanId`  | Span ID of the server span, 16 hex chars | `b7ad6b7169203331`                 |

> Headers are **only added when a valid trace context exists**. If the span context is invalid (e.g., tracing is disabled), no headers are appended.

## Compatibility

| Extension Version | OTel Java Agent | Java Runtime | Frameworks                                      |
|-------------------|-----------------|--------------|-------------------------------------------------|
| 2.25.0            | 2.25.0+         | 11+          | Spring Boot, Quarkus, Micronaut, Servlet, etc.  |

The extension works with **any HTTP framework** that the OTel Java Agent instruments — no framework-specific configuration needed.

## Build from Source

### Prerequisites

- JDK 11+
- Gradle 9.x (wrapper included)

### Build

```bash
git clone https://github.com/weworkto/opentelemetry-javaagent-extensions.git
cd opentelemetry-javaagent-extensions
./gradlew build
```

Output: `build/libs/opentelemetry-javaagent-extensions-2.25.0.jar`

### Test

```bash
./gradlew test
```

## Project Structure

```
opentelemetry-javaagent-extensions/
├── build.gradle.kts                          # Gradle build config (Kotlin DSL)
├── gradle/
│   └── libs.versions.toml                    # Centralized dependency version management
├── settings.gradle.kts
├── src/
│   ├── main/java/.../TraceIdResponseCustomizer.java       # Extension implementation
│   └── test/java/.../TraceIdResponseCustomizerTest.java   # Unit tests
└── .github/workflows/release.yml             # CI: tag push → build → GitHub Release
```

## How to Release

Push a version tag to trigger automated build & release:

```bash
git tag v2.25.0
git push origin v2.25.0
```

GitHub Actions will build the JAR and create a [Release](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases) with the artifact attached.

## Version Strategy

The extension version tracks the OTel Java Agent version it's built against. When upgrading the OTel Agent:

1. Update `otelAgent` and `otelSdk` in `gradle/libs.versions.toml`
2. Run `./gradlew build` to verify compatibility
3. Tag and release

## Background

This project is a modernized reimplementation based on the [Alibaba Cloud OpenTelemetry Best Practice](https://github.com/alibabacloud-observability/opentelemetry-best-practice) guide: [How to put TraceId into HTTP response automatically](https://github.com/alibabacloud-observability/opentelemetry-best-practice/blob/main/how-to-put-traceId-into-http-response-automatically.md).

Key improvements over the original:

- **Updated to OTel Java Agent 2.x** (original was built for 1.28.0)
- **Uses `extension-api` instead of full agent JAR** as compile dependency (lightweight)
- **Uses unshaded OpenTelemetry API imports** (as recommended by 2.x)
- **Adds `SpanContext.isValid()` check** to avoid injecting invalid trace IDs
- **Automated CI/CD** via GitHub Actions with release artifact publishing
- **Unit tested** with JUnit 5

## References

- [OpenTelemetry Java Agent Extensions Guide](https://opentelemetry.io/docs/zero-code/java/agent/extensions/)
- [`HttpServerResponseCustomizer` SPI source](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/javaagent-extension-api/src/main/java/io/opentelemetry/javaagent/bootstrap/http/HttpServerResponseCustomizer.java)
- [OTel Java Agent Extension API on Maven Central](https://central.sonatype.com/artifact/io.opentelemetry.javaagent/opentelemetry-javaagent-extension-api)
- [Alibaba Cloud OpenTelemetry Best Practice](https://github.com/alibabacloud-observability/opentelemetry-best-practice) - original tutorial and reference implementation
- [How to put TraceId into HTTP response automatically (tutorial)](https://github.com/alibabacloud-observability/opentelemetry-best-practice/blob/main/how-to-put-traceId-into-http-response-automatically.md)
