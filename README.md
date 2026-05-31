# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section01` branch.** First Spring AI lesson — a Spring Boot app that chats with a **local LLM via [Ollama](https://ollama.com)** through Spring AI's `ChatClient`.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### This branch — `section01`

**Purpose**: introduce Spring AI's `ChatClient` against a **local Ollama** backend. The app exposes `GET /api/chat?message=...` and forwards the message to a locally hosted LLM, returning the response as plain text.

**What it adds on top of the [`section0`](https://github.com/david-iaggbs/spring-ai/tree/section0) baseline**:
- `spring-ai-bom` + `spring-ai-starter-model-ollama` dependencies.
- `spring.ai.model.chat=ollama` and `spring.ai.ollama.chat.options.model=llama3.2:1b` in `application.properties`.
- Replaces the baseline `HelloController` with a `ChatController` that injects `ChatClient.Builder`.
- Adds a Testcontainers-based end-to-end test (`ChatControllerOllamaIT`) gated behind a Maven `e2e` profile, plus the Testcontainers dependencies (`spring-boot-testcontainers`, `spring-ai-spring-boot-testcontainers`, `testcontainers:ollama`, `junit-jupiter`).

Run `git diff section0 section01` to see exactly what this lesson costs in code, config and dependencies.

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.ollama`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **Tests**: at minimum a `@SpringBootTest` context-load test plus a `@WebMvcTest` slice test for the controller. Lessons that introduce external services add an `@Tag("e2e")` Testcontainers IT, gated behind an `e2e` Maven profile.

---

## 🚀 How to Run (section01/ollama)

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
cd section01/ollama
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 3. Send a chat request

The app exposes a single endpoint:

| Method | URL | Query param | Response |
|--------|-----|-------------|----------|
| `GET`  | `http://localhost:8080/api/chat` | `message` (required) | `text/plain` — the LLM's reply |

**With curl:**

```bash
curl "http://localhost:8080/api/chat?message=Hello%2C%20who%20are%20you%3F"
```

**With httpie:**

```bash
http ":8080/api/chat" message=="Hello, who are you?"
```

**In a browser:** open <http://localhost:8080/api/chat?message=Hello>

**With Postman:** import `SpringAI.postman_collection.json` from the repo root — it contains a prebuilt request for this endpoint.

The first request may take a few seconds while Ollama loads the model into memory; subsequent requests are faster.

> Endpoint defined in `section01/ollama/src/main/java/com/eazybytes/ollama/controller/ChatController.java`. Port `8080` is the Spring Boot default — override with `--server.port=9090` if it conflicts.

### Configuration

All AI settings live in `section01/ollama/src/main/resources/application.properties`:

```properties
spring.ai.model.chat=ollama
spring.ai.ollama.chat.options.model=llama3.2:1b
```

Swap `llama3.2:1b` for any other model you have pulled in Ollama (e.g. `llama3.2:3b`, `mistral`, `qwen2.5`) to experiment.

### Running the tests

Two layers of tests are wired up:

**Fast tests (default)** — JUnit slice test of the controller with a mocked `ChatClient`, plus a context-load test. No infrastructure needed.

```bash
cd section01/ollama
./mvnw test
```

Runs in ~2 s and is safe to run in CI without Podman/Docker.

**End-to-end test (`e2e` profile)** — boots an Ollama container via Testcontainers, pulls `llama3.2:1b`, and hits `/api/chat` against the real model.

```bash
# 1. Start the Podman machine and expose its socket (one-time per shell session)
podman machine start
export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
export TESTCONTAINERS_RYUK_DISABLED=true   # Ryuk isn't supported on rootless Podman

# 2. Run the e2e suite
./mvnw test -Pe2e
```

First run takes a few minutes (image + model pull); subsequent runs are faster. The IT class is tagged `e2e` and excluded from the default Surefire run.

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀  
