# 🧰 Technology Stack Specification: Spring AI 2.0 & Model Context Protocol (MCP)

A comprehensive technical inventory of all frameworks, runtimes, persistence layers, protocol standards, and infrastructure components utilized in this application.

---

## 1. Core Runtime & Language

| Technology | Version | Purpose & Strategic Rationale |
| :--- | :--- | :--- |
| **Java** | `21 LTS` | Standard enterprise long-term support release. Leverages Java 21 Records for immutable DTOs/Structured Output schemas, pattern matching, enhanced switch expressions, and virtual threads readiness. |
| **OpenJDK Runtime** | `Eclipse Temurin 21` | High-performance, certified open-source distribution used in local builds and production multi-stage container images. |

---

## 2. Application, AI & Protocol Frameworks

| Layer | Framework / Module | Version | Details |
| :--- | :--- | :--- | :--- |
| **Core Framework** | `Spring Boot` | `4.1.1` | The cloud-native Java microservice backbone. Provides dependency injection, auto-configuration, and integrated HTTP server. |
| **Base Framework** | `Spring Framework` | `7.0.x` | Modern reactive and servlet foundation with updated Jakarta EE specifications. |
| **AI Framework** | `Spring AI BOM` | `2.0.1` | Official Spring project for enterprise AI orchestration, memory advisors, and tool calling. |
| **LLM Starter** | `spring-ai-starter-model-google-genai` | `2.0.1` | Unified Google starter bridging Spring AI with Gemini Developer API and Google Cloud Vertex AI. |
| **MCP Protocol Starter** | `spring-ai-starter-mcp-client` | `2.0.1` | Spring AI 2.0 native client starter implementing Anthropic's Model Context Protocol (MCP) specification. |
| **LLM Model** | `Google Gemini 2.5 Flash` | Default | High-speed, multimodal, low-latency foundation model with advanced reasoning and tool-use capabilities. |

---

## 3. Persistence & Database Tier

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Database Engine** | `PostgreSQL` | `16.15` | Enterprise-grade ACID-compliant relational database. |
| **Vector Extension** | `pgvector` | `0.8.6` | Extends PostgreSQL with vector similarity search (IVFFlat, HNSW) for Retrieval-Augmented Generation (RAG) and embeddings. |
| **ORM / Data Access** | `Spring Data JPA` | Managed by Boot | Repositories with derived query methods, declarative transaction management, and connection pooling. |
| **Persistence Provider**| `Hibernate ORM` | `6.x / 7.x` | High-performance object-relational mapping with automated schema updates (`ddl-auto: update`). |
| **Connection Pool** | `HikariCP` | Bundled | Industry-standard, ultra-fast zero-overhead JDBC connection pool. |

---

## 4. Infrastructure & Containerization

| Tool | Specification | Role |
| :--- | :--- | :--- |
| **Docker Engine / Podman**| Standard Linux daemon | Container runtime powering database isolation and repeatable local development environments. |
| **Docker Compose** | Compose v2 schema | Declarative service orchestration managing container dependencies, volumes (`pgdata`), and health checks. |
| **PostgreSQL Image** | `pgvector/pgvector:pg16` | Pre-compiled Debian-based image bundling PostgreSQL 16 and native pgvector C extensions. |
| **PostgreSQL MCP Server** | `mcp/postgres:latest` | Standardized Model Context Protocol server exposing PostgreSQL schema and query tools over JSON-RPC. |
| **Application Image** | Multi-stage Dockerfile | Stage 1: Maven build (`maven:3.9.9-eclipse-temurin-21`). Stage 2: Minimal Alpine JRE (`eclipse-temurin:21-jre-alpine`). |

---

## 5. Build, Test & Integration Tooling

| Category | Tool | Description |
| :--- | :--- | :--- |
| **Build Management** | `Apache Maven 3.9+` | Handles declarative dependency graphs, BOM imports, compile lifecycle, and Spring Boot executable jar packaging. |
| **Local Maven Wrapper** | `./mvnw` (v3.9.9) | Project-local wrapper script ensuring zero-installation reproducible builds. |
| **HTTP Testing** | `cURL` + `jq` | Scripted endpoint verification and JSON formatting via `test-endpoints.sh`. |
| **Validation API** | `Jakarta Validation` | `@NotNull`, `@NotBlank`, and `@Size` constraints on incoming DTOs. |
| **Observability** | `SLF4J` + `Logback` | Configured with detailed DEBUG tracing for `org.springframework.ai` tool execution loops and Hibernate SQL statements. |

---

## 6. Enterprise Security, Governance & Production Hardening Stack

| Security Domain | Technology / Specification | Standard & Production Role |
| :--- | :--- | :--- |
| **Input Validation** | `Jakarta Bean Validation 3.0` | `@NotBlank`, `@Size`, `@Pattern` enforcing payload hygiene before prompt execution. |
| **Prompt Sandboxing** | `Spring AI ChatClient Fluent API` | Complete isolation between system instructions, model context, and dynamic user prompts. |
| **Output Type Safety** | `Java 21 Records` + Jackson 3 | Strongly typed structured output schema enforcement via `.entity()` preventing script/code execution. |
| **Container Hardening** | `Alpine Linux + Non-Root User` | Drops root privileges; executes JVM under dedicated `USER spring:spring` (UID/GID 10001). |
| **Secret Management** | `Externalized Environment Injection` | Zero hardcoded keys. Vault / AWS Secrets Manager / GCP Secret Manager compatible. |
| **Transport Encryption** | `TLS 1.3 / SSL` | Enforces HTTPS on public APIs and `sslmode=verify-full` on PostgreSQL persistence links. |
| **Threat Model Alignment** | `OWASP LLM Top 10 (2025/2026)` | Proactive mitigation against Prompt Injection (LLM01), Insecure Output (LLM02), and Excessive Agency (LLM08). |
| **Supply Chain Security** | `CycloneDX / SPDX SBOM` | Software Bill of Materials tracking, automated Trivy scanning, and Dependabot vulnerability patching. |
| **Audit & Governance** | `SLF4J / Logback Masked Logging` | Audit trail of all database tool invocations with automated PII masking. |

> 🛡️ For the operational policy, CVSS triage SLAs, and vulnerability reporting procedures, refer to **[SECURITY.md](./SECURITY.md)**.

