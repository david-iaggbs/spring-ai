# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section6` branch.** *Tool Calling*: the AI assistant can now **take real actions** — not just answer questions. `TimeTools` exposes Java methods the LLM can invoke to get the current local or zoned time. `HelpDeskTools` lets the LLM create and query JPA-backed support tickets on behalf of the authenticated user, using `ToolContext` to pass the `username` from the HTTP header into the tool without exposing it in the prompt. Built on top of [`section5`](https://github.com/david-iaggbs/spring-ai/tree/section5) and running entirely on a **local LLM via [Ollama](https://ollama.com)** (`llama3.2:1b` for chat, `nomic-embed-text` for embeddings) — no API key required.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### This branch — `section6`

**Purpose**: introduce **AI Tool Calling** (also called Function Calling). The assistant built in section5 could answer from documents and the web; now it can **act** — invoking real Java methods to get live data and write to a database on behalf of the user.

**What it adds on top of the [`section5`](https://github.com/david-iaggbs/spring-ai/tree/section5) baseline** — a fresh `section06/springai` module (dropping `section05/springai`):

- **`tools/TimeTools.java`** — two `@Tool`-annotated methods: `getCurrentLocalTime()` (no args, returns the server's local time) and `getCurrentTime(String timeZone)` (accepts any IANA zone ID). Registered as `defaultTools` on the `timeChatClient` bean so they are always available to the model without per-request wiring.

- **`tools/HelpDeskTools.java`** — two `@Tool` methods backed by JPA: `createTicket(TicketRequest, ToolContext)` persists a new `HelpDeskTicket` row and returns a confirmation string directly to the user (`returnDirect = true`); `getTicketStatus(ToolContext)` fetches all tickets for the calling user. Both receive a `ToolContext` injected at request time — the `username` is read from the context map rather than from the prompt, preventing prompt-injection attacks.

- **`entity/HelpDeskTicket.java`** / **`service/HelpDeskTicketService.java`** / **`repository/HelpDeskTicketRepository.java`** — Lombok-annotated JPA entity, service, and `JpaRepository` that the tools delegate to. `HelpDeskTicket` carries `id`, `username`, `issue`, `status`, `createdAt`, and `eta`.

- **`config/TimeChatClientConfig.java`** — builds the `timeChatClient` bean with `defaultTools(timeTools)` + `SimpleLoggerAdvisor` + `MessageChatMemoryAdvisor` + `TokenUsageAuditAdvisor`. Every prompt on this client automatically has access to the time tools.

- **`config/HelpDeskChatClientConfig.java`** — builds the `helpDeskChatClient` bean with `defaultSystem(helpDeskSystemPromptTemplate)` + `defaultTools(timeTools)` + `defaultAdvisors(SimpleLoggerAdvisor, MessageChatMemoryAdvisor, TokenUsageAuditAdvisor)`. The `HelpDeskTools` bean is **not** a default tool here — it is added per-request in the controller so the controller can also supply the `ToolContext`. A `DefaultToolExecutionExceptionProcessor` bean is included (commented out) for instructional reference — uncomment it to have Spring AI turn tool exceptions into model-visible error messages instead of propagating them.

- **`controller/TimeController.java`** / **`controller/HelpDeskController.java`** — endpoints under `/api/tools`:

  | Endpoint | `ChatClient` | What it demonstrates |
  |----------|-------------|----------------------|
  | `GET /api/tools/local-time` | `timeChatClient` | Tool registered at build time via `defaultTools`; no per-request context needed |
  | `GET /api/tools/help-desk` | `helpDeskChatClient` | Tool added per-request (`.tools(helpDeskTools)`) with per-request context (`.toolContext(Map.of("username", username))`) |

  Both endpoints accept `username` **header** (→ `CONVERSATION_ID` for memory, `username` in `ToolContext`) and `message` query param.

- **`promptTemplates/helpDeskSystemPromptTemplate.st`** — StringTemplate system prompt that defines the helpdesk persona, tool-use policy ("check for existing tickets before creating a new one"), and response style.

- **New dependencies**: `spring-boot-starter-data-jpa`, `lombok` (Lombok annotations on the entity).

Run `git diff section5 section6` to see exactly what this lesson adds in code, config and dependencies.

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.springai`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **LLM runtime**: all branches run entirely on **[Ollama](https://ollama.com)** — no API keys needed for the model. From section5 onward, a second Ollama model (`nomic-embed-text`) is also required for embeddings.

---

## 🚀 How to Run (section06/springai)

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (`<java.version>21</java.version>`) | Temurin / Zulu / Oracle |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global install needed |
| **Podman** (or Docker) | Runs Ollama + starts Qdrant via `compose.yml` | Tested with Podman; Docker works with the same commands |
| **Ollama runtime** | Chat (`llama3.2:1b`) and embeddings (`nomic-embed-text`) | Run as a container (see step 1) |
| **`TAVILY_SEARCH_API_KEY`** | `/api/rag/web-search/chat` endpoint only (carried over from section5) | Free tier at [tavily.com](https://tavily.com); not needed for the new tool endpoints |

No OpenAI API key needed. Chat and embeddings both run locally via Ollama.

### 1. Start Ollama in a container (Podman)

```bash
# create a named volume so models persist across restarts
podman volume create ollama

# run the Ollama server, exposing it on localhost:11434
podman run -d --name ollama \
  -p 11434:11434 \
  -v ollama:/root/.ollama \
  docker.io/ollama/ollama

# pull the chat model — must support tool calling
podman exec -it ollama ollama pull llama3.2:1b

# pull the embedding model — used to vectorise the HR PDF and query text
podman exec -it ollama ollama pull nomic-embed-text
```

> On Apple Silicon, the container runs CPU-only (Metal GPU acceleration is not available inside Podman/Docker). For small models like `llama3.2:1b` and `nomic-embed-text` this is fine; for larger models, prefer a native Ollama install.
>
> On Linux with an NVIDIA GPU, add `--device nvidia.com/gpu=all` (Podman) or `--gpus=all` (Docker).

To verify it's up:

```bash
curl http://localhost:11434/api/tags
```

### 2. Start the Spring Boot app

```bash
cd section06/springai
./mvnw spring-boot:run
```

Spring Boot detects `compose.yml` and **starts Qdrant automatically** before the app context is created. On first run, `HRPolicyLoader` reads `Eazybytes_HR_Policies.pdf`, splits it into chunks, embeds them with `nomic-embed-text` via Ollama, and stores them in Qdrant — this takes a few seconds. Subsequent runs skip the re-ingestion (Qdrant persists its data inside the container volume while it's running; restart the container to reset it).

The app starts on `http://localhost:8080`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:~/chatmemory`, user: `madan`, password: `12345`). Chat memory and `helpdesk_tickets` are both stored in this H2 database.

> **Podman users:** point Spring Boot at the Podman socket before running:
> ```bash
> podman machine start
> export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
> ./mvnw spring-boot:run
> ```

### 3. Query the tool endpoints

The new endpoints are under `/api/tools`, all accepting `username` **header** and `message` query param:

| Method | URL | What happens |
|--------|-----|--------------|
| `GET` | `/api/tools/local-time` | Model invokes `TimeTools.getCurrentLocalTime()` or `getCurrentTime(zone)` and returns the result |
| `GET` | `/api/tools/help-desk` | Model decides whether to call `createTicket` or `getTicketStatus`; tickets are persisted in H2 |

**With curl:**

```bash
# Ask for the current time — the model calls getCurrentLocalTime()
curl -H "username: alice" \
  "http://localhost:8080/api/tools/local-time?message=What%20time%20is%20it%3F"

# Ask for a specific timezone — the model calls getCurrentTime("Asia/Tokyo")
curl -H "username: alice" \
  "http://localhost:8080/api/tools/local-time?message=What%20time%20is%20it%20in%20Tokyo%3F"

# Create a support ticket — the model calls createTicket() and the row appears in H2
curl -H "username: alice" \
  "http://localhost:8080/api/tools/help-desk?message=I%20cannot%20access%20the%20VPN%2C%20please%20create%20a%20ticket"

# Check open tickets — the model calls getTicketStatus()
curl -H "username: alice" \
  "http://localhost:8080/api/tools/help-desk?message=What%20are%20my%20open%20tickets%3F"
```

> **Seeing tool calling in action:** advisors log at DEBUG (`logging.level.org.springframework.ai.chat.client.advisor=DEBUG` in `application.properties`), so you can watch the model's tool-call requests and the tool results being returned.

> **createTicket and returnDirect:** when `returnDirect = true` the tool result string is returned straight to the HTTP response without a second model round-trip — you'll see exactly what `HelpDeskTools.createTicket()` returned, not a paraphrase.

### Configuration

All settings live in `section06/springai/src/main/resources/application.properties`:

```properties
# Model — must support tool calling
spring.ai.ollama.chat.options.model=llama3.2:1b
spring.ai.ollama.embedding.options.model=nomic-embed-text

# Conversation memory + helpdesk tickets: file-based H2 so history survives restarts
spring.datasource.url=jdbc:h2:file:~/chatmemory;AUTO_SERVER=true
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
spring.ai.chat.memory.repository.jdbc.schema=classpath:/schema/schema-h2db.sql

# Qdrant vector store (started automatically via compose.yml)
spring.ai.vectorstore.qdrant.initialize-schema=true
spring.ai.vectorstore.qdrant.host=localhost
spring.ai.vectorstore.qdrant.port=6334
spring.ai.vectorstore.qdrant.collection-name=eazybytes
```

Swap `llama3.2:1b` for any tool-capable model you have pulled in Ollama (e.g. `llama3.2:3b`, `mistral-nemo`).

### Running the tests

```bash
cd section06/springai

# Unit + integration tests (no Ollama or Podman needed — AI and Qdrant are mocked)
./mvnw test

# Full pyramid including E2E tests (requires Podman/Docker — Testcontainers starts Ollama + Qdrant)
./mvnw test -Pe2e
```

The `-Pe2e` profile removes the `@Tag("e2e")` exclusion so `ToolsControllerOllamaIT` runs — it spins up `ollama/ollama:latest` and `qdrant/qdrant:latest` in containers, pulls both models, and exercises the tool endpoints end-to-end.

```bash
# Podman: forward the socket before running E2E tests
ssh -i ~/.local/share/containers/podman/machine/machine -p <port> \
  -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile=/dev/null \
  -fN -L /tmp/tc-podman.sock:/run/user/501/podman/podman.sock core@127.0.0.1

export DOCKER_HOST=unix:///tmp/tc-podman.sock TESTCONTAINERS_RYUK_DISABLED=true
JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-26.jdk/Contents/Home \
  ./mvnw test -Pe2e
```

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀
