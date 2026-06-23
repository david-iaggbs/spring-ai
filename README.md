# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section4` branch.** *Chat Memory*: by default a `ChatClient` is stateless — every request starts from a blank slate. This branch makes the assistant **remember the conversation**: a `MessageChatMemoryAdvisor` backed by a `JdbcChatMemoryRepository` (H2) transparently loads the prior turns for a conversation, prepends them to the prompt, and saves the new exchange back. The `username` HTTP header is the conversation id, so each user gets an isolated, **persistent** history that survives app restarts. Built on top of [`section2.8`](https://github.com/david-iaggbs/spring-ai/tree/section2.8) and still running entirely on a **local LLM via [Ollama](https://ollama.com)**.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### This branch — `section4`

**Purpose**: introduce **Chat Memory**. Every lesson so far treated each request as independent — the model never knew what you asked a moment ago. This branch wires Spring AI's chat-memory stack so the assistant carries context across requests within a conversation.

**What it adds on top of the [`section2.8`](https://github.com/david-iaggbs/spring-ai/tree/section2.8) baseline** — a fresh `section04/springai` module (dropping `section02/springai`):

- **`config/ChatMemoryChatClientConfig.java`** — builds the memory stack in three layers:

  | Layer | Type | Role |
  |-------|------|------|
  | Storage | `JdbcChatMemoryRepository` | Auto-configured by the JDBC starter; persists one row per message in `SPRING_AI_CHAT_MEMORY` (H2). |
  | Policy  | `MessageWindowChatMemory` (`maxMessages(10)`) | Keeps only the most recent 10 messages per conversation so the prompt can't grow without bound. |
  | Plumbing | `MessageChatMemoryAdvisor` | On every call, loads the conversation's prior messages, prepends them to the prompt, and saves the new user + assistant pair back. |

  The single `ChatClient` bean sets no persona — the lesson is the memory round-trip, so the assistant answers freely (e.g. "What is my name?") rather than refusing. A `SimpleLoggerAdvisor` is also attached so you can watch the prior turns being stitched into each prompt at DEBUG.

- **`controller/ChatMemoryController.java`** — one endpoint:

  | Endpoint | Inputs | Returns |
  |----------|--------|---------|
  | `GET /api/chat-memory` | `username` **header** (→ `CONVERSATION_ID`), `message` query param | `text/plain` — a reply that takes the conversation history into account |

  The `username` header is passed per call via `advisors(spec -> spec.param(CONVERSATION_ID, username))`, so one stateless `ChatClient` serves many concurrent users — each with a fully isolated history.

- **`resources/schema/schema-h2db.sql`** — idempotent (`CREATE … IF NOT EXISTS`) H2 DDL for the chat-memory table, run on startup. Idempotency keeps it safe to re-run against the **file-based** H2 database, so history survives restarts.

- **New dependencies**: `spring-ai-starter-model-chat-memory-repository-jdbc` and `com.h2database:h2`.

- **Tests** (same three-tier pyramid as the previous branches):
  - **Unit** — `ChatMemoryControllerTest` (`@WebMvcTest`) stubs a deep-stub `ChatClient` and asserts the reply is returned; also asserts a missing `username` header is a `400`.
  - **Integration** — `ChatMemoryControllerIntegrationTest` (`@SpringBootTest`, mocked `ChatClient`, isolated in-memory H2) **captures the per-call advisor customizer** and replays it to verify the controller binds `CONVERSATION_ID` to the `username` header value.
  - **E2E** — `ChatMemoryControllerOllamaIT` boots a real Ollama Testcontainer and, after a turn, **queries `ChatMemory` directly** to prove the user + assistant messages were persisted and retrievable by conversation id — a deterministic check that doesn't depend on what a 1 B model actually says. A two-turn "both succeed" test covers the happy path; the aspirational *recall* test (model answers using a fact from an earlier turn) is `@Disabled` because `llama3.2:1b` is too small to recall reliably — point the model at e.g. `llama3.1:8b` to re-enable.

Run `git diff section2.8 section4` to see exactly what this lesson costs in code, config and tests.

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.springai`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **Test pyramid**: every controller ships **unit** (`@WebMvcTest`), **integration** (`@SpringBootTest` with a mocked `ChatClient`), and **e2e** (`@Tag("e2e")` Testcontainers IT, gated behind the `e2e` Maven profile).

---

## 🚀 How to Run (section04/springai)

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (`<java.version>21</java.version>`) | Temurin / Zulu / Oracle |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global install needed |
| **Podman** (or Docker) | Runs the Ollama container | Tested with Podman; Docker works with the same commands |
| **Ollama runtime** | Serves the LLM at `http://localhost:11434` | Run as a container (see step 1) |
| **An LLM model** | `application.properties` references `llama3.2:1b` (~1.3 GB) | Pulled into Ollama in step 1 |

No API keys, vector DBs, or external services are needed. Conversation memory uses an **embedded H2 database** (file `~/chatmemory.mv.db`) that Spring Boot creates automatically — nothing to install.

### 1. Start Ollama in a container (Podman)

```bash
# create a named volume so models persist across restarts
podman volume create ollama

# run the Ollama server, exposing it on localhost:11434
podman run -d --name ollama \
  -p 11434:11434 \
  -v ollama:/root/.ollama \
  docker.io/ollama/ollama

# pull the model the app is configured to use
podman exec -it ollama ollama pull llama3.2:1b
```

> On Apple Silicon, the container runs CPU-only (Metal GPU acceleration is not available inside Podman/Docker). For small models like `llama3.2:1b` this is fine; for larger models, prefer a native Ollama install.
>
> On Linux with an NVIDIA GPU, add `--device nvidia.com/gpu=all` (Podman) or `--gpus=all` (Docker).

To verify it's up:

```bash
curl http://localhost:11434/api/tags
```

### 2. Start the Spring Boot app

```bash
cd section04/springai
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` and creates the `SPRING_AI_CHAT_MEMORY` table in `~/chatmemory.mv.db` on first run.

### 3. Have a conversation with memory

The app exposes one endpoint:

| Method | URL | Inputs | Response |
|--------|-----|--------|----------|
| `GET`  | `http://localhost:8080/api/chat-memory` | `username` **header** (conversation id), `message` query param | `text/plain` — a reply that remembers earlier turns from the same `username` |

**With curl** — two requests sharing the same `username` show memory at work:

```bash
# Turn 1 — state a fact.
curl -H "username: madan03" \
  "http://localhost:8080/api/chat-memory?message=My%20name%20is%20Madan.%20Please%20remember%20it."

# Turn 2 — the model recalls it from history (use a capable model for reliable recall).
curl -H "username: madan03" \
  "http://localhost:8080/api/chat-memory?message=What%20is%20my%20name%3F"
```

Use a **different** `username` and the history is empty — conversations are fully isolated.

**With Postman:** import `SpringAI.postman_collection.json` from the repo root (see the **Section04 → ChatMemory** request).

> **Persistence:** memory lives in file-based H2 (`~/chatmemory`), so a conversation survives an app restart. Delete `~/chatmemory.mv.db` to wipe all history. The schema DDL (`classpath:/schema/schema-h2db.sql`) is idempotent, so `initialize-schema=always` is safe across restarts.

> **Seeing memory in action:** the advisors log at DEBUG (`logging.level.org.springframework.ai.chat.client.advisor=DEBUG` in `application.properties`), so you can watch the prior turns being stitched into each prompt.

### Configuration

All settings live in `section04/springai/src/main/resources/application.properties`:

```properties
# Model
spring.ai.model.chat=ollama
spring.ai.ollama.chat.options.model=llama3.2:1b

# Conversation memory: file-based H2 so history survives restarts
spring.datasource.url=jdbc:h2:file:~/chatmemory;AUTO_SERVER=true
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
spring.ai.chat.memory.repository.jdbc.schema=classpath:/schema/schema-h2db.sql
```

Swap `llama3.2:1b` for any other model you have pulled in Ollama (e.g. `llama3.2:3b`, `mistral`, `qwen2.5`) — a larger model recalls facts from history far more reliably.

### Running the tests

Three layers of tests are wired up:

**Fast tests (default)** — unit slice (`@WebMvcTest`), integration (`@SpringBootTest` with a mocked `ChatClient` and isolated in-memory H2), and a context-load test. No infrastructure needed.

```bash
cd section04/springai
./mvnw test
```

Runs in a few seconds and is safe to run in CI without Podman/Docker.

**End-to-end test (`e2e` profile)** — boots an Ollama container via Testcontainers, pulls `llama3.2:1b`, drives `/api/chat-memory`, and asserts the conversation was persisted to (an in-memory) H2.

```bash
# 1. Start the Podman machine and expose its socket (one-time per shell session)
podman machine start
export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
export TESTCONTAINERS_RYUK_DISABLED=true   # Ryuk isn't supported on rootless Podman

# 2. Run the e2e suite
./mvnw test -Pe2e
```

First run takes ~30 s (image + model pull); subsequent runs are faster. The IT class is tagged `e2e` and excluded from the default Surefire run.

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀
