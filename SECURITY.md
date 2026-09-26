# 🛡️ Enterprise Security Policy & Production Hardening Standard

> **Document Version:** 1.0.0  
> **Status:** Active / Production Baseline  
> **Classification:** Public / Enterprise Standard  
> **Target Frameworks:** Spring AI 2.0.1, Spring Boot 4.1.1, Google Gemini, Model Context Protocol (MCP), PostgreSQL 16 (pgvector)

---

## 1. Security Philosophy & Standards Alignment

This project implements a **Defense-in-Depth** and **Zero Trust Architecture (ZTA)** approach tailored for enterprise AI-assisted systems of record. By decoupling probabilistic reasoning (LLMs) from deterministic data operations (PostgreSQL), we enforce rigorous boundaries across the application stack.

Our security controls adhere to leading industry frameworks:
- **OWASP Top 10 for Large Language Model Applications (2025/2026)**
- **NIST AI Risk Management Framework (AI RMF 1.0)**
- **CIS Benchmarks for PostgreSQL 16 & Linux Containers**
- **SOC 2 Type II & ISO/IEC 27001 Trust Services Criteria (Confidentiality, Integrity, Availability)**
- **GDPR / CCPA Data Minimization & Privacy Principles**

---

## 2. Supported Versions & Patch Management Lifecycle

We maintain active security updates and vulnerability triage for the following baseline versions:

| Component | Minimum Version | Target Production Version | Security Support Status | Patch SLA |
| :--- | :--- | :--- | :--- | :--- |
| **Java Runtime** | `21.0.x LTS` | Eclipse Temurin `21.0.6+` | Active Support | Critical: 72 hrs |
| **Spring Boot** | `4.1.0` | `4.1.1` | Active Support | Critical: 72 hrs / High: 14 days |
| **Spring AI BOM** | `2.0.0` | `2.0.1` | Active Support | Critical: 72 hrs / High: 14 days |
| **Google Gemini API** | `gemini-2.0-flash` | `gemini-3.1-flash-lite` | Active Support | Managed by Google Cloud |
| **PostgreSQL** | `15.x` | `16.15` (pgvector 0.8.6) | Active Support | Standard DB maintenance |
| **MCP Client Starter** | `2.0.0` | `2.0.1` | Active Support | Active |

---

## 3. Coordinated Vulnerability Disclosure (CVD)

We take the security of our application, infrastructure, and underlying AI orchestrations seriously. If you discover a potential security vulnerability, please report it responsibly.

### 3.1 Reporting Protocol
- **Email:** Send full vulnerability details to **`security@company.internal`** (or open a private GitHub Security Advisory).
- **Encryption:** Use our public PGP key (available upon request or in security keyrings) for sensitive proofs of concept.
- **Do Not:** 
  - File public GitHub issues for undisclosed security flaws.
  - Perform Denial of Service (DoS) attacks on production or staging environments.
  - Attempt unauthorized data exfiltration or modification of customer records.

### 3.2 Triage & Remediation SLAs

| Severity Level (CVSS v3.1 / v4.0) | Initial Acknowledgment | Triage & Assessment | Patch Release Target |
| :--- | :--- | :--- | :--- |
| **Critical** (CVSS 9.0 - 10.0) | < 12 Hours | < 24 Hours | < 72 Hours |
| **High** (CVSS 7.0 - 8.9) | < 24 Hours | < 48 Hours | < 7 Days |
| **Medium** (CVSS 4.0 - 6.9) | < 48 Hours | < 5 Business Days | Next Release Cycle (< 30 Days) |
| **Low** (CVSS 0.1 - 3.9) | < 72 Hours | < 10 Business Days | Planned Maintenance |

---

## 4. AI & LLM Threat Mitigation (OWASP LLM Top 10)

The table below outlines our proactive controls against generative AI threat vectors:

| Threat (OWASP LLM) | Attack Vector | Project Implementation & Mitigating Control |
| :--- | :--- | :--- |
| **LLM01: Prompt Injection & Jailbreaks** | Adversary attempts to override system prompt instructions to execute unintended actions or bypass restrictions. | **Strict Tool Contracts:** Gemini cannot execute arbitrary SQL. It can only call strongly typed Java methods annotated with `@Tool`.<br>**Input Boundary Delimitation:** User prompts are isolated from system instructions in `ChatClient.prompt()`. |
| **LLM02: Insecure Output Handling** | Unchecked LLM output executed downstream by the application or rendered raw to browsers. | **Structured Output Records:** Uses Java 21 Records (`CustomerInsight`) parsed via Spring AI's `.entity(CustomerInsight.class)`. Unstructured text is validated before consumption. |
| **LLM04: Model Denial of Service** | Volumetric token flooding, recursive tool invocations, or computationally prohibitive prompts. | **Token Limits & HTTP Timeouts:** Model temperature capped (`0.7`), HTTP connection timeouts (5s connect / 15s read), and rate limiting policies on API endpoints. |
| **LLM06: Sensitive Information Disclosure** | Accidental leakage of PII, proprietary source code, or confidential customer telemetry in AI completions. | **Data Minimization:** Database tools only project required customer fields (`name`, `email`, `plan`, `notes`), omitting authentication tokens or sensitive hashes.<br>**Zero Retention API Policy:** Google GenAI enterprise endpoints do not use prompt telemetry for foundation model training. |
| **LLM07: Insecure Plugin / Tool Design** | AI tools exposed with excessive permissions or vulnerable parameter parsing. | **Principle of Least Privilege (PoLP):** `@Tool` methods enforce validation on parameters (`@ToolParam`). Database queries utilize JPA parameterized criteria, eliminating SQL injection. |
| **LLM08: Excessive Agency** | Autonomous agent performing destructive actions (DELETE, UPDATE, DROP) without human authorization. | **Read-Only Agent Boundaries:** MCP and `@Tool` database tools are restricted exclusively to idempotent read queries (`findByName`, `findByPlan`, `findAll`). Destructive actions require explicit REST endpoints with human approval. |

---

## 5. Production Hardening & Security Controls

### 5.1 Secrets Management & Credentials Isolation
1. **Zero Hardcoded Secrets Policy & Pre-Commit Protection:** 
   No passwords, API tokens, or private keys may ever be committed to git. This is actively enforced in the repository via two complementary layers:
   - **Native Git Pre-Commit Hook ([`.githooks/pre-commit`](./.githooks/pre-commit)):** A zero-dependency scanner that inspects staged file diffs (`git diff --cached`) before every commit for Google Gemini API keys (`AIzaSy...`), RSA/EC private keys, AWS access keys, and hardcoded credentials. Activated across your local workspace via:
     ```bash
     git config core.hooksPath .githooks
     ```
   - **Turnkey Gitleaks Integration ([`.pre-commit-config.yaml`](./.pre-commit-config.yaml)):** Configured with official `gitleaks` rules and hygiene checks for developers using the `pre-commit` framework:
     ```bash
     pre-commit install
     ```
2. **Environment Variable Injection:**
   - Production API Key: `SPRING_AI_GOOGLE_GENAI_API_KEY`
   - Production Database Credentials: `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_URL`
3. **Enterprise Key Stores:** In production deployments (Kubernetes, AWS ECS, GCP Cloud Run), secrets must be dynamically injected via:
   - **HashiCorp Vault** (via Spring Cloud Vault)
   - **AWS Secrets Manager** or **GCP Secret Manager**
   - **Kubernetes SealedSecrets / External Secrets Operator**
4. **Vertex AI Workload Identity:** For GCP cloud deployments, authenticate via IAM Workload Identity Federation instead of static API keys.

### 5.2 Model Context Protocol (MCP) Security Baseline
1. **Containerized Sandboxing:** When running standalone MCP servers (e.g. `mcp/postgres`), containers must run in an isolated Docker network (`internal_bridge`) with no direct public internet egress.
2. **Schema & JSON-RPC Validation:** All incoming MCP tool requests are validated against strict JSON Schema definitions before reaching the Spring context.
3. **Transport Security:** MCP server-to-client communication over SSE (Server-Sent Events) or WebSockets must be secured with TLS 1.3 (`https://` / `wss://`).

### 5.3 Data Persistence Tier Hardening (PostgreSQL + pgvector)
1. **Network Segregation:** PostgreSQL port `5432` must **never** be exposed directly to public subnets. It should only be accessible from application containers within a private Virtual Private Cloud (VPC).
2. **TLS / SSL In Transit:** Enforce `sslmode=verify-full` in `SPRING_DATASOURCE_URL` for production connections:
   ```properties
   spring.datasource.url=jdbc:postgresql://db.internal:5432/spring_ai_db?sslmode=verify-full&sslrootcert=/etc/ssl/certs/db-ca.crt
   ```
3. **Role-Based Access Control (RBAC):**
   - The application connects using a dedicated unprivileged user (`spring_app_user`), not the superuser `postgres`.
   - The user has permissions restricted to `SELECT`, `INSERT`, `UPDATE` on the `customers` table only.
   - `DROP`, `ALTER`, and `TRUNCATE` privileges are strictly revoked.
4. **Vector Embeddings Protection:** Vector stores managed in PostgreSQL via `pgvector` inherit relational Row-Level Security (RLS) policies to prevent cross-tenant vector leakage.

### 5.4 Container & Runtime Security
1. **Non-Root Execution:** Containers must run under an unprivileged user and group (`USER spring:spring`, UID/GID 10001). Root execution inside production containers is prohibited.
2. **Minimal Attack Surface:** Container images are built on `eclipse-temurin:21-jre-alpine` or Google Distroless, containing no package managers, shells, or unneeded binaries.
3. **Resource Limits:** Docker Compose / Kubernetes manifests must enforce strict memory and CPU limits (`limits: { cpus: "2.0", memory: "2Gi" }`) to prevent resource exhaustion attacks.
4. **Immutable Filesystem:** Mount container root filesystems as read-only (`read_only: true`), allowing write access only to designated temporary directories (`/tmp`).

### 5.5 Observability, Auditability & SIEM Compliance
1. **Structured Security Logging:** Log events are formatted using SLF4J / Logback with structured JSON outputs compatible with Datadog, Splunk, Elastic, and Google Cloud Logging.
2. **PII Masking in Logs:** Hibernate SQL parameters and raw AI prompts containing customer email addresses or names are masked in production log streams.
3. **Audit Trail for MCP Actions:** Every execution of `@Tool` or MCP calls records:
   - Timestamp (UTC ISO-8601)
   - Tool Identifier (`getCustomersByPlan`, `getCustomerByEmail`)
   - Caller Subject / Correlation ID
   - Execution Status & Duration
   - *Never logs raw database password or authorization headers.*

---

## 6. Supply Chain Security & CI/CD Pipeline Controls

To guarantee provenance and dependency integrity:
1. **Automated Vulnerability Scanning:** 
   - **Trivy / Grype:** Container image vulnerability scanning during CI builds.
   - **OWASP Dependency-Check / Snyk:** Continuous analysis of Maven dependencies.
   - **GitHub Dependabot:** Automated patch PRs for transitive dependency updates.
2. **Software Bill of Materials (SBOM):**
   - Generated during Maven package lifecycle using CycloneDX (`cyclonedx-maven-plugin`).
3. **Deterministic Reproducible Builds:** Dependency versions locked via Maven parent BOM and exact plugin configurations.

---

## 7. Production Security Readiness Checklist

Before promoting any instance of this service to production, complete the following verification:

- [ ] All environment credentials (`SPRING_AI_GOOGLE_GENAI_API_KEY`, database passwords) externalized to Secret Vault.
- [ ] Database connection configured with `sslmode=require` or `sslmode=verify-full` in target VPC.
- [x] Application container configured to run as unprivileged user `spring:spring` ([Dockerfile](./Dockerfile)).
- [ ] Public network exposure of PostgreSQL port `5432` disabled in production network topology.
- [x] Debug logging levels transitioned to `INFO` / `WARN` and error disclosure masked via production profile ([application-prod.yml](./src/main/resources/application-prod.yml)).
- [x] CORS policies restricted to designated enterprise domains ([WebSecurityConfig.java](./src/main/java/com/example/springai/config/WebSecurityConfig.java)).
- [x] Jakarta Bean Validation boundary constraints enforced across customer records and AI prompts ([Customer.java](./src/main/java/com/example/springai/model/Customer.java), [ChatRequest.java](./src/main/java/com/example/springai/dto/ChatRequest.java)).
- [x] Global exception hygiene masking internal system details and sanitizing errors ([GlobalExceptionHandler.java](./src/main/java/com/example/springai/exception/GlobalExceptionHandler.java)).
- [ ] Automated rate limiting and WAF (Web Application Firewall) enabled at API gateway layer.
- [ ] Dependency vulnerability scan executed with zero Critical or High CVEs unmitigated.
