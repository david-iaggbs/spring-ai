# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section2.0` branch.** First lesson of *Spring AI Essentials*: **Message Roles in LLMs** — the `system` prompt steers the model into a specific persona, while the `user` prompt carries the question. Built on top of [`section1`](https://github.com/david-iaggbs/spring-ai/tree/section1) and still running entirely on a **local LLM via [Ollama](https://ollama.com)** through Spring AI's `ChatClient`.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### Section 2 — Spring AI Essentials (incremental sub-branches)

The "Spring AI Essentials" course section is split into incremental sub-branches `section2.0` → `section2.8`. Each one **drops the previous section's module**, adds **one concept**, ships the full **unit + integration + e2e** test pyramid, and stays runnable end-to-end.

| Branch | Concept |
|--------|---------|
| **`section2.0`** *(you are here)* | **Message Roles** — `system` vs `user` |
| `section2.1` | Spring AI Defaults (`defaultSystem`, `defaultUser`, `defaultOptions`) |
| `section2.2` | Prompt Templates (`.st` files) |
| `section2.3` | Prompt Stuffing |
| `section2.4` | Built-in Advisors (`SimpleLoggerAdvisor`) |
| `section2.5` | Custom Advisors (`TokenUsageAuditAdvisor`) |
| `section2.6` | ChatOptions (global + per-call overrides) |
| `section2.7` | Streaming Responses (`Flux<String>`) |
| `section2.8` | Structured Output (Bean / List / Map / `ParameterizedTypeReference`) |

### This branch — `section2.0`

**Purpose**: introduce **Message Roles**. The `ChatController` no longer sends only the raw user message; it explicitly sets a **system** role describing the assistant's persona ("internal IT helpdesk assistant") and a **user** role carrying the employee's question.

**What it adds on top of the [`section1`](https://github.com/david-iaggbs/spring-ai/tree/section1) baseline**:
- Drops `section01/ollama/` and scaffolds a new module `section02/springai/` (`com.eazybytes.springai`).
- `ChatController` uses `chatClient.prompt().system(IT_HELPDESK_SYSTEM_PROMPT).user(message).call().content()` instead of the bare `chatClient.prompt(message).call().content()` from `section1`.
- Adds a **three-layer test pyramid** per controller:
  - **Unit** — `ChatControllerTest` (`@WebMvcTest` + deep-stubbed `ChatClient.Builder`).
  - **Integration** — `ChatControllerIntegrationTest` (`@SpringBootTest` with a `@TestConfiguration` overriding the auto-configured prototype `ChatClient.Builder` with a singleton mock).
  - **E2E** — `ChatControllerOllamaIT` (`@Testcontainers` + real `OllamaContainer`, `@Tag("e2e")`, gated behind the `e2e` Maven profile).

Run `git diff section1 section2.0` to see exactly what this lesson costs in code, config and tests.

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.springai`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **Test pyramid (from `section2.0` onward)**: every controller ships **unit** (`@WebMvcTest`), **integration** (`@SpringBootTest` with mocked `ChatClient.Builder`), and **e2e** (`@Tag("e2e")` Testcontainers IT, gated behind the `e2e` Maven profile).

---

## 🚀 How to Run (section02/springai)

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (`<java.version>21</java.version>`) | Temurin / Zulu / Oracle |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global install needed |
| **Podman** (or Docker) | Runs the Ollama container | Tested with Podman; Docker works with the same commands |
| **Ollama runtime** | Serves the LLM at `http://localhost:11434` | Run as a container (see step 1) |
| **An LLM model** | `application.properties` references `llama3.2:1b` (~1.3 GB) | Pulled into Ollama in step 1 |

No API keys, vector DBs, or external services are needed for this branch — everything runs locally and offline.

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
cd section02/springai
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 3. Send a chat request

The app exposes a single endpoint:

| Method | URL | Query param | Response |
|--------|-----|-------------|----------|
| `GET`  | `http://localhost:8080/api/chat` | `message` (required) | `text/plain` — the LLM's reply, scoped to the IT-helpdesk persona |

**With curl:**

```bash
curl "http://localhost:8080/api/chat?message=I%20forgot%20my%20password"
```

**With httpie:**

```bash
http ":8080/api/chat" message=="I forgot my password"
```

**In a browser:** open <http://localhost:8080/api/chat?message=I%20forgot%20my%20password>

**With Postman:** import `SpringAI.postman_collection.json` from the repo root.

Because the system prompt constrains the assistant to IT support, off-topic questions ("recommend me a pizza recipe") will be politely declined — that's the whole point of the **system role**.

> Endpoint defined in `section02/springai/src/main/java/com/eazybytes/springai/controller/ChatController.java`. Port `8080` is the Spring Boot default — override with `--server.port=9090` if it conflicts.

### Configuration

All AI settings live in `section02/springai/src/main/resources/application.properties`:

```properties
spring.ai.model.chat=ollama
spring.ai.ollama.chat.options.model=llama3.2:1b
```

Swap `llama3.2:1b` for any other model you have pulled in Ollama (e.g. `llama3.2:3b`, `mistral`, `qwen2.5`) to experiment.

### Running the tests

Three layers of tests are wired up:

**Fast tests (default)** — unit slice (`@WebMvcTest`), integration (`@SpringBootTest` with mocked `ChatClient.Builder`), and a context-load test. No infrastructure needed.

```bash
cd section02/springai
./mvnw test
```

Runs in ~3 s and is safe to run in CI without Podman/Docker.

**End-to-end test (`e2e` profile)** — boots an Ollama container via Testcontainers, pulls `llama3.2:1b`, and hits `/api/chat` against the real model.

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
