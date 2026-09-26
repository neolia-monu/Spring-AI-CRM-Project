# Spring AI 2.0 & Google Gemini: Model Context Protocol (MCP) & PostgreSQL Hub

> A production-grade enterprise reference architecture implementing **Spring AI 2.0**, **Google Gemini**, and **PostgreSQL (pgvector)** on top of **Spring Boot 4.x** and **Java 21**, featuring the **Model Context Protocol (MCP)** and declarative database tool calling.

---

## 📌 Executive Summary

Modern enterprise applications require generative AI capabilities that are not merely conversational toys, but deeply integrated with corporate systems of record. 

This project demonstrates how to connect **Spring AI 2.0** with **Google Gemini (`gemini-3.1-flash-lite`)** and **PostgreSQL** running in Docker, utilizing the **Model Context Protocol (MCP)** standard:
1. **MCP (Model Context Protocol):** Standardizes how Gemini LLM interfaces with external database tools, schema inspection, and data retrieval.
2. **Deterministic DB Grounding:** Gemini autonomously invokes database tools (`@Tool` / MCP) to retrieve verified SQL records before answering, eliminating hallucinations.
3. **Enterprise Architecture:** Combines Spring Data JPA persistence with a modular MCP tool calling layer.

---

## 📑 In-Depth Documentation Links

For deep dives into design decisions, implementation specs, and technology choices, refer to the dedicated documentation pages below:

| Document | Description |
| :--- | :--- |
| 🔄 **[CHANGELOG.md](./CHANGELOG.md)** | Detailed audit of all updates, including Spring Boot 4.1.1 baseline, Spring AI 2.0.1, and MCP client integration. |
| 🏛️ **[ARCHITECTURE.md](./ARCHITECTURE.md)** | Senior Tech Lead breakdown of software architecture, MCP vs MCR, Agentic ReAct loops, and design patterns. |
| 🧰 **[TECH_STACK.md](./TECH_STACK.md)** | Comprehensive inventory of technologies, frameworks, MCP starters, runtime specifications, and infrastructure choices. |

---

## ⚡ Quickstart: How to Download & Run

### Prerequisites
- **Docker** / **Podman** (for PostgreSQL and MCP containers)
- **Java 21 LTS**
- **Apache Maven 3.9+** (configured in PATH or via `./mvnw`)
- **Google Gemini API Key** (Free tier available at [Google AI Studio](https://aistudio.google.com/apikey))

---

### Step 1: Start PostgreSQL (Docker)
From the project root:
```bash
docker compose up -d
```
Verify the container is healthy:
```bash
docker ps
```
The database starts on `localhost:5432` with pre-configured credentials (`postgres` / `postgrespassword`) and automatic seed data.

*(Optional: To also launch the standalone PostgreSQL MCP Server container, run `docker compose --profile mcp up -d`)*.

---

### Step 2: Configure Gemini Credentials
Export your Gemini API Key in your shell:
```bash
export SPRING_AI_GOOGLE_GENAI_API_KEY="AIzaSyYourGeminiApiKeyHere..."
```

---

### Step 3: Build & Launch with Maven
Run the Spring Boot application:
```bash
mvn clean spring-boot:run
# OR using the Maven wrapper:
# ./mvnw clean spring-boot:run
```

---

### Step 4: Verify & Test APIs
Run the included test script:
```bash
./test-endpoints.sh
```

Or execute direct `curl` commands:

1. **Standard PostgreSQL Query:**
   ```bash
   curl -s http://localhost:8080/api/customers
   ```

2. **Basic Gemini Chat (Zero-shot LLM):**
   ```bash
   curl -s -X POST http://localhost:8080/api/ai/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Explain in two sentences what Spring AI 2.0 brings to Java developers."}'
   ```

3. **Agentic Database Tool Calling (Gemini dynamically queries PostgreSQL):**
   ```bash
   curl -s -X POST http://localhost:8080/api/ai/chat-with-db \
     -H "Content-Type: application/json" \
     -d '{"message": "List all customers who have an Enterprise plan and summarize their requirements."}'
   ```

4. **Type-Safe Structured Output (LLM to Java Record):**
   ```bash
   curl -s http://localhost:8080/api/ai/insights/1
   ```
