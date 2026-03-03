# OpenTelemetry Java Agent Extension

[![Build & Release](https://github.com/weworkto/opentelemetry-javaagent-extensions/actions/workflows/release.yml/badge.svg)](https://github.com/weworkto/opentelemetry-javaagent-extensions/actions/workflows/release.yml)
[![GitHub Release](https://img.shields.io/github/v/release/weworkto/opentelemetry-javaagent-extensions?label=release)](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/latest)
[![OTel Agent](https://img.shields.io/badge/OTel_Java_Agent-2.25.0-blueviolet)](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/tag/v2.25.0)
[![Java](https://img.shields.io/badge/Java-11%2B-orange)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3.1-02303A?logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/github/license/weworkto/opentelemetry-javaagent-extensions)](LICENSE)

**[English](README_EN.md)** | **中文**

一个轻量级的 [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) 扩展，**自动将 TraceId 和 SpanId 注入 HTTP 响应头**，实现前后端链路追踪关联，无需修改任何业务代码。

---

## 工作原理

本扩展实现了 OTel Java Agent 提供的 [`HttpServerResponseCustomizer`](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/javaagent-extension-api/src/main/java/io/opentelemetry/javaagent/bootstrap/http/HttpServerResponseCustomizer.java) SPI 接口，拦截每个 HTTP 服务端响应并追加当前链路上下文信息：

```
HTTP/1.1 200 OK
Content-Type: application/json
TraceId: 0af7651916cd43dd8448eb211c80319c
SpanId: b7ad6b7169203331
```

部署后，你可以：

- 从任意 HTTP 响应中复制 `TraceId`，到追踪后端（Jaeger、Tempo 等）中搜索对应链路
- 在前端错误上报中自动携带 Trace ID，快速定位后端问题
- 将客户端异常与服务端分布式链路进行关联

## 快速开始

### 1. 下载

从 [Releases](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/latest) 获取最新 JAR：

```bash
curl -fsSL -o opentelemetry-javaagent-extensions.jar \
  "https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/download/v2.25.0/opentelemetry-javaagent-extensions-2.25.0.jar"
```

### 2. 启动应用

```bash
java -javaagent:/path/to/opentelemetry-javaagent.jar \
     -Dotel.javaagent.extensions=/path/to/opentelemetry-javaagent-extensions.jar \
     -jar your-app.jar
```

### 3. 验证

```bash
curl -si http://localhost:8080/api/health | grep -E "TraceId|SpanId"
# TraceId: 0af7651916cd43dd8448eb211c80319c
# SpanId: b7ad6b7169203331
```

## Docker 集成

在 Dockerfile 中与 OTel Java Agent 一起下载：

```dockerfile
ARG OTEL_AGENT_VERSION=2.25.0
ARG OTEL_EXT_VERSION=2.25.0

RUN curl -fsSL -o /agent/opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar"

RUN curl -fsSL -o /agent/opentelemetry-javaagent-extensions.jar \
    "https://github.com/weworkto/opentelemetry-javaagent-extensions/releases/download/v${OTEL_EXT_VERSION}/opentelemetry-javaagent-extensions-${OTEL_EXT_VERSION}.jar"
```

## Kubernetes 部署

```yaml
env:
  - name: JAVA_TOOL_OPTIONS
    value: >-
      -javaagent:/agent/opentelemetry-javaagent.jar
      -Dotel.javaagent.extensions=/agent/opentelemetry-javaagent-extensions.jar
      -Dotel.service.name=my-service
      -Dotel.exporter.otlp.endpoint=http://otel-collector:4317
```

## 响应头说明

| 响应头    | 说明                          | 示例                               |
|-----------|-------------------------------|------------------------------------|
| `TraceId` | W3C Trace ID，32 位十六进制    | `0af7651916cd43dd8448eb211c80319c` |
| `SpanId`  | 当前服务端 Span ID，16 位十六进制 | `b7ad6b7169203331`                 |

> 仅在存在有效链路上下文时才会添加响应头。如果 Span 上下文无效（例如追踪被禁用），则不会注入任何头信息。

## 兼容性

| 扩展版本 | OTel Java Agent | Java 运行时 | 支持框架                              |
|----------|-----------------|-------------|---------------------------------------|
| 2.25.0   | 2.25.0+         | 11+         | Spring Boot、Quarkus、Micronaut、Servlet 等 |

本扩展适用于 OTel Java Agent 支持的**所有 HTTP 框架**，无需针对特定框架做配置。

## 从源码构建

### 环境要求

- JDK 11+
- Gradle 9.x（项目已包含 Wrapper）

### 构建

```bash
git clone https://github.com/weworkto/opentelemetry-javaagent-extensions.git
cd opentelemetry-javaagent-extensions
./gradlew build
```

产物：`build/libs/opentelemetry-javaagent-extensions-2.25.0.jar`

### 测试

```bash
./gradlew test
```

## 项目结构

```
opentelemetry-javaagent-extensions/
├── build.gradle.kts                          # Gradle 构建配置 (Kotlin DSL)
├── gradle/
│   └── libs.versions.toml                    # 依赖版本统一管理
├── settings.gradle.kts
├── src/
│   ├── main/java/.../TraceIdResponseCustomizer.java       # 扩展实现
│   └── test/java/.../TraceIdResponseCustomizerTest.java   # 单元测试
└── .github/workflows/release.yml             # CI: 推送 tag → 构建 → 发布 Release
```

## 如何发布

推送版本 tag 即可触发自动构建和发布：

```bash
git tag v2.25.0
git push origin v2.25.0
```

GitHub Actions 会自动构建 JAR 并创建 [Release](https://github.com/weworkto/opentelemetry-javaagent-extensions/releases)。

## 版本策略

扩展版本与 OTel Java Agent 版本保持一致。升级 OTel Agent 时：

1. 更新 `gradle/libs.versions.toml` 中的 `otelAgent` 和 `otelSdk` 版本号
2. 运行 `./gradlew build` 验证兼容性
3. 打 tag 发布

## 项目背景

本项目基于[阿里云 OpenTelemetry 最佳实践](https://github.com/alibabacloud-observability/opentelemetry-best-practice)中的教程 [《如何将 TraceId 自动写入 HTTP 响应头》](https://github.com/alibabacloud-observability/opentelemetry-best-practice/blob/main/how-to-put-traceId-into-http-response-automatically.md) 进行了现代化重新实现。

相比原版的改进：

- **升级到 OTel Java Agent 2.x**（原版基于 1.28.0 构建）
- **使用轻量级 `extension-api`** 替代完整 Agent JAR 作为编译依赖
- **使用非 shaded 的 OpenTelemetry API 导入**（2.x 推荐方式）
- **添加 `SpanContext.isValid()` 检查**，避免注入无效 Trace ID
- **GitHub Actions 自动化 CI/CD**，tag 推送即发布
- **JUnit 5 单元测试** 覆盖

## 参考资料

- [OpenTelemetry Java Agent 扩展开发指南](https://opentelemetry.io/docs/zero-code/java/agent/extensions/)
- [`HttpServerResponseCustomizer` SPI 源码](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/javaagent-extension-api/src/main/java/io/opentelemetry/javaagent/bootstrap/http/HttpServerResponseCustomizer.java)
- [OTel Java Agent Extension API (Maven Central)](https://central.sonatype.com/artifact/io.opentelemetry.javaagent/opentelemetry-javaagent-extension-api)
- [阿里云 OpenTelemetry 最佳实践](https://github.com/alibabacloud-observability/opentelemetry-best-practice) - 原始教程和参考实现
- [如何将 TraceId 自动写入 HTTP 响应头（教程）](https://github.com/alibabacloud-observability/opentelemetry-best-practice/blob/main/how-to-put-traceId-into-http-response-automatically.md)
