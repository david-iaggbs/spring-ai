# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section2.3` branch.** Fourth lesson of *Spring AI Essentials*: **Prompt Stuffing** — inject a small knowledge base (FAQ, policies, product info) directly into the **system** prompt so the LLM answers grounded in *your* data, no fine-tuning or vector store required. Works great for tiny KBs; for anything larger, RAG (section 4) is the right answer. Built on top of [`section2.2`](https://github.com/david-iaggbs/spring-ai/tree/section2.2) and still running entirely on a **local LLM via [Ollama](https://ollama.com)**.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### Section 2 — Spring AI Essentials (incremental sub-branches)

The "Spring AI Essentials" course section is split into incremental sub-branches `section2.0` → `section2.8`. Each one **drops the previous section's module**, adds **one concept**, ships the full **unit + integration + e2e** test pyramid, and stays runnable end-to-end.

| Branch | Concept |
|--------|---------|
| `section2.0` | Message Roles — `system` vs `user` |
| `section2.1` | Spring AI Defaults — `defaultSystem`, `defaultUser` |
| `section2.2` | Prompt Templates — external `.st` files, `param(...)` binding |
| **`section2.3`** *(you are here)* | **Prompt Stuffing** — domain knowledge inside the `system` prompt |
| `section2.4` | Built-in Advisors (`SimpleLoggerAdvisor`) |
| `section2.5` | Custom Advisors (`TokenUsageAuditAdvisor`) |
| `section2.6` | ChatOptions (global + per-call overrides) |
| `section2.7` | Streaming Responses (`Flux<String>`) |
| `section2.8` | Structured Output (Bean / List / Map / `ParameterizedTypeReference`) |

### This branch — `section2.3`

**Purpose**: introduce **Prompt Stuffing**. Instead of fine-tuning a model or wiring a vector store, you cram the relevant snippets of your domain knowledge directly into the **system** prompt — the LLM then answers grounded in that data. This branch ships a tiny EazyBytes-Tech FAQ as a `.st` resource and an endpoint that answers customer questions from it.

> **Token-budget warning.** Prompt stuffing scales linearly with knowledge-base size. It's perfect for ~1 KB of policies/FAQs (this branch). It will hit context-window limits and cost the moment your KB grows beyond a few thousand tokens — at that point switch to **RAG** (covered later in the course).

**What it adds on top of the [`section2.2`](https://github.com/david-iaggbs/spring-ai/tree/section2.2) baseline**:
- New template `src/main/resources/promptTemplates/systemPromptTemplate.st` containing the company FAQ (office hours, refund policy, course access, enterprise plan…).
- New `PromptStuffingController` exposing `GET /api/prompt-stuffing?message=…`. It injects the singleton `ChatClient`, calls `.system(Resource)` with the stuffed FAQ, and forwards the user's question. The system prompt instructs the model to answer **only** from the provided context.
- The previous endpoints from sections 2.1 and 2.2 are unchanged.
- Tests for the new endpoint:
  - **Unit** — `@WebMvcTest(PromptStuffingController.class)` with `@MockitoBean(RETURNS_DEEP_STUBS) ChatClient`, asserting the response body.
  - **Integration** — full context + `ArgumentCaptor<Resource>` on `.system(...)`; the captured resource is read back from the classpath and its contents are asserted to contain key phrases from the stuffed FAQ.
  - **E2E** — Ollama Testcontainer call to `/api/prompt-stuffing` asking "What is your refund policy?", asserting a non-empty body.

Run `git diff section2.2 section2.3` to see exactly what this lesson costs in code, config and tests.

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

The app exposes three endpoints:

| Method | URL | Query params | Response |
|--------|-----|-------------|----------|
| `GET`  | `http://localhost:8080/api/chat`             | `message` (required) | `text/plain` — HR-persona reply using the default system prompt |
| `GET`  | `http://localhost:8080/api/email`            | `customerName`, `customerMessage` (both required) | `text/plain` — a customer-service email draft, rendered from `userPromptTemplate.st` |
| `GET`  | `http://localhost:8080/api/prompt-stuffing`  | `message` (required) | `text/plain` — answer grounded in the EazyBytes-Tech FAQ stuffed into `systemPromptTemplate.st` |

**With curl:**

```bash
# Section 2.1 — HR defaults.
curl "http://localhost:8080/api/chat?message=How%20many%20vacation%20days%20do%20I%20get?"

# Section 2.2 — template-driven user prompt.
curl "http://localhost:8080/api/email?customerName=Alice&customerMessage=My%20order%20arrived%20damaged."

# Section 2.3 — answer from the stuffed FAQ.
curl "http://localhost:8080/api/prompt-stuffing?message=What%20is%20your%20refund%20policy?"
```

**With Postman:** import `SpringAI.postman_collection.json` from the repo root.

Both `.st` templates live under `section02/springai/src/main/resources/promptTemplates/`. Edit `systemPromptTemplate.st` to swap the stuffed knowledge base; no code change needed.

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
