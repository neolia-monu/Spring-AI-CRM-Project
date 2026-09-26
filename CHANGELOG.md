# 🔄 Change Log & Migration Record

All notable changes, architectural upgrades, and version migrations for this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.1] - 2026-09-26

### 🚀 Major Version Upgrade: Spring Boot 4.1.1 & Spring AI 2.0 Baseline

#### 1. Spring Boot Parent Upgrade (`pom.xml`)
- **Diff:**
  ```diff
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
  -       <version>3.4.3</version>
  +       <version>4.1.1</version>
          <relativePath/>
      </parent>
  ```
- **Rationale:** Spring AI 2.0.0+ requires **Spring Boot 4.x** and **Spring Framework 7.0**. Retaining Spring Boot 3.x with Spring AI 2.0 introduces classpath incompatibilities due to updated Jakarta EE packages, JSpecify null-safety annotations, and Jackson 3 serialization pipelines.

---

### 🌐 Model Context Protocol (MCP) Integration
- **Added Dependency:** Included `spring-ai-starter-mcp-client` (version `2.0.1`) in `pom.xml`.
- **Architectural Clarification:** Clarified difference between **MCR** (internal Model-Controller-Repository code layout) and **MCP** (Model Context Protocol standard for AI-to-database tool execution).
- **PostgreSQL MCP Server in Docker:** Updated `docker-compose.yml` to include the `postgres-mcp` service profile for running official MCP database servers.

---

### 📦 Key Architectural Changes in Spring AI 2.0

#### 1. Unified Google GenAI Starter
- **Previous (1.x):** Required choosing between `spring-ai-starter-model-vertex-ai-gemini` (heavy GCP credentials SDK) or community wrappers.
- **Current (2.0.x):** Unified into `spring-ai-starter-model-google-genai`. It supports both:
  - **Gemini Developer API:** Configured via simple lightweight API key (`spring.ai.google.genai.api-key`).
  - **Vertex AI Enterprise:** Configured via GCP project and location parameters without modifying code.
- **Provider Activation:** Spring AI 2.0 requires explicit declaration of the active chat model in configuration:
  ```properties
  spring.ai.model.chat=google-genai
  ```

#### 2. Declarative Tool Calling (`@Tool` & `@ToolParam`)
- **Previous (1.x):** Tool calling execution loops were coupled inside model client implementations.
- **Current (2.0.x):** Tool calling is decoupled and placed into `ChatClient`'s composable `ToolCallingAdvisor` chain. Any Spring Bean method can be exposed to Gemini simply by annotating it with `@Tool`:
  ```java
  @Component
  public class DatabaseCustomerTools {
      @Tool(description = "Search customers by subscription tier")
      public List<Customer> getCustomersByPlan(
          @ToolParam(description = "Plan name: Free, Pro, Enterprise") String plan
      ) {
          return customerRepository.findByPlanIgnoreCase(plan);
      }
  }
  ```

#### 3. Native Model Context Protocol (MCP) Foundation
- Spring AI 2.0 introduces native support for Anthropic's Model Context Protocol (MCP), establishing standardized client/server contracts (`@McpTool`) that allow Spring Boot services to act as MCP tool providers or consumers.

#### 4. Type-Safe Structured Output Schema Validation
- The `ChatClient` fluent builder now natively serializes Gemini JSON outputs directly into Java records or DTOs:
  ```java
  CustomerInsight insight = chatClient.prompt()
      .user(...)
      .call()
      .entity(CustomerInsight.class);
  ```

---

### 🗄️ Database & Persistence Integration
- **PostgreSQL 16 + pgvector:** Added containerized PostgreSQL instance (`pgvector/pgvector:pg16`) via `docker-compose.yml`.
- **MCR Layer:** Structured clean separation between `Customer` entity (`@Entity`), `CustomerRepository` (`JpaRepository`), and `CustomerController` (`@RestController`).
- **Automated DDL & Seed Data:** Added `src/main/resources/data.sql` to initialize enterprise customer records for zero-setup local demonstration.

---

### 🔍 Verification & Compatibility Matrix

| Component | Target Version | Supported Range | Compatibility Notes |
| :--- | :--- | :--- | :--- |
| **Java** | `21 LTS` | `21` - `23` | Mandatory for Spring Boot 4 bytecode baseline. |
| **Spring Boot** | `4.1.1` | `>= 4.0.0` | Upgraded to support Spring AI 2.x BOM. |
| **Spring AI BOM** | `2.0.1` | `>= 2.0.0` | Provides unified starters, MCP starters, and ToolCallingAdvisor. |
| **MCP Starter** | `2.0.1` | `>= 2.0.0` | `spring-ai-starter-mcp-client` enabled. |
| **Google Gemini Model** | `gemini-2.5-flash` | `gemini-2.0-flash`, `gemini-2.5-pro` | Default model configured in `application.yml`. |
| **PostgreSQL** | `16.15` | `15` - `17` | `pgvector` extension v0.8.6 enabled. |
