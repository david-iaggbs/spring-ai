# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section2.8` branch.** Ninth lesson of *Spring AI Essentials*: **Structured Output** — stop parsing free-form text. `ChatClient.call().entity(...)` and friends coerce the LLM's reply into a typed Java object (`Bean`, `List<String>`, `Map`, `List<POJO>`) by appending JSON-schema or list-format instructions to the prompt and deserializing the response automatically. Built on top of [`section2.7`](https://github.com/david-iaggbs/spring-ai/tree/section2.7) and still running entirely on a **local LLM via [Ollama](https://ollama.com)**. This is the **final branch** of the *Spring AI Essentials* learning path.

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
| `section2.3` | Prompt Stuffing — domain knowledge inside the `system` prompt |
| `section2.4` | Built-in Advisors — `SimpleLoggerAdvisor` via `defaultAdvisors(...)` |
| `section2.5` | Custom Advisors — `TokenUsageAuditAdvisor` implementing `CallAdvisor` |
| `section2.6` | ChatOptions — global `defaultOptions(...)` + per-call `OllamaOptions` override |
| `section2.7` | Streaming Responses — `Flux<String>` over `text/event-stream` |
| **`section2.8`** *(you are here)* | **Structured Output** — `entity(Class)`, `ListOutputConverter`, `MapOutputConverter`, `ParameterizedTypeReference` |

### This branch — `section2.8`

**Purpose**: introduce **Structured Output**. So far every endpoint returned plain text. This branch adds a controller that calls `.entity(...)` on `ChatClient`, telling Spring AI which Java type the response should be deserialized into. Spring AI appends format instructions to the prompt (a JSON schema for `Bean` / `List<POJO>`, a comma-separated-list instruction for `ListOutputConverter`, a generic JSON object instruction for `MapOutputConverter`), then runs the LLM's reply through Jackson to populate the target type.

**What it adds on top of the [`section2.7`](https://github.com/david-iaggbs/spring-ai/tree/section2.7) baseline**:
- New record `model/CountryCities.java` (`country`, `cities`) used as the target POJO.
- New `StructuredOutPutController` exposing four endpoints:

  | Endpoint | Converter | Returns |
  |----------|-----------|---------|
  | `GET /api/chat-bean`      | `entity(CountryCities.class)`                                     | a single POJO |
  | `GET /api/chat-list`      | `entity(LIST_CONVERTER)` (`ListOutputConverter`)                  | a `List<String>` |
  | `GET /api/chat-map`       | `entity(MAP_CONVERTER)` (`MapOutputConverter`)                    | a `Map<String,Object>` |
  | `GET /api/chat-bean-list` | `entity(COUNTRY_CITIES_LIST_TYPE)` (`ParameterizedTypeReference`) | a `List<CountryCities>` |

  The controller injects the singleton `ChatClient` from `ChatClientConfig` but **overrides the HR default system prompt** with `NEUTRAL_SYSTEM_PROMPT` per call, so off-topic schema-shaped requests aren't refused. Converters are exposed as `static final` constants so tests can reference the exact same instance.
- Tests:
  - **Unit** — `StructuredOutPutControllerTest` stubs each of the four `.entity(...)` overloads on a deep-stub `ChatClient` and asserts the JSON response shape.
  - **Integration** — `StructuredOutPutControllerIntegrationTest` repeats the pattern with the full context loaded and adds a `verify(...).entity(<same converter instance>)` to lock in the converter wiring per endpoint.
  - **E2E** — `StructuredOutPutControllerOllamaIT` runs the four endpoints against the Ollama Testcontainer and asserts `200 OK` — a successful response proves Spring AI was able to deserialize the model's reply into the requested Java type, which is the lesson here. The `List<POJO>` case is `@Disabled` because a 1 B-parameter model does not reliably emit a top-level JSON array of objects; bump the model to e.g. `llama3.1:8b` to re-enable.

Run `git diff section2.7 section2.8` to see exactly what this lesson costs in code, config and tests.

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

The app exposes eight endpoints:

| Method | URL | Query params | Response |
|--------|-----|-------------|----------|
| `GET`  | `http://localhost:8080/api/chat`             | `message` (required) | `text/plain` — HR-persona reply using the default system prompt |
| `GET`  | `http://localhost:8080/api/email`            | `customerName`, `customerMessage` (both required) | `text/plain` — a customer-service email draft, rendered from `userPromptTemplate.st` |
| `GET`  | `http://localhost:8080/api/prompt-stuffing`  | `message` (required) | `text/plain` — answer grounded in the EazyBytes-Tech FAQ stuffed into `systemPromptTemplate.st` |
| `GET`  | `http://localhost:8080/api/stream`           | `message` (required) | `text/event-stream` — tokens streamed live as the model generates them |
| `GET`  | `http://localhost:8080/api/chat-bean`        | `message` (required) | `application/json` — a single `CountryCities` POJO |
| `GET`  | `http://localhost:8080/api/chat-list`        | `message` (required) | `application/json` — a `List<String>` |
| `GET`  | `http://localhost:8080/api/chat-map`         | `message` (required) | `application/json` — a `Map<String,Object>` |
| `GET`  | `http://localhost:8080/api/chat-bean-list`   | `message` (required) | `application/json` — a `List<CountryCities>` |

**With curl:**

```bash
# Section 2.1 — HR defaults.
curl "http://localhost:8080/api/chat?message=How%20many%20vacation%20days%20do%20I%20get?"

# Section 2.2 — template-driven user prompt.
curl "http://localhost:8080/api/email?customerName=Alice&customerMessage=My%20order%20arrived%20damaged."

# Section 2.3 — answer from the stuffed FAQ.
curl "http://localhost:8080/api/prompt-stuffing?message=What%20is%20your%20refund%20policy?"

# Section 2.7 — watch tokens stream in real time (-N disables curl output buffering).
curl -N "http://localhost:8080/api/stream?message=Tell%20me%20about%20parental%20leave"

# Section 2.8 — structured output: get a typed JSON object back.
curl "http://localhost:8080/api/chat-bean?message=Tell%20me%20the%20country%20France%20and%20three%20of%20its%20largest%20cities."
curl "http://localhost:8080/api/chat-list?message=List%20three%20primary%20colours."
curl "http://localhost:8080/api/chat-map?message=Give%20me%20an%20object%20with%20'country'%20and%20'capital'%20for%20France."
```

**With Postman:** import `SpringAI.postman_collection.json` from the repo root.

Both `.st` templates live under `section02/springai/src/main/resources/promptTemplates/`. Edit `systemPromptTemplate.st` to swap the stuffed knowledge base; no code change needed.

> **Seeing the advisor in action:** by default `SimpleLoggerAdvisor` logs at DEBUG. To watch every request/response live, add this to `application.properties` (or pass it as `--logging.level.…=DEBUG` on the command line):
>
> ```properties
> logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=DEBUG
> ```

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
