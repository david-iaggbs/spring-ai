# From Java Dev to AI Engineer: Spring AI Fast Track

## 🌱 Spring AI Course – Resources & Reference Links

Welcome to the official GitHub repository for the **Spring AI Course**. This course helps you build intelligent applications using the Spring AI framework and integrate powerful LLMs like OpenAI into your Spring Boot apps.

Below are some carefully curated reference links and tools used throughout the course. Bookmark this information for quick access during development and exploration.

---

## 📘 Official Documentation

- **[Spring AI Official Documentation](https://docs.spring.io/spring-ai/reference/index.html)**  
  The core reference for understanding Spring AI modules, configuration, and supported AI providers.

- **[OpenAI Platform Docs](https://platform.openai.com/docs/overview)**  
  Learn how to use OpenAI's APIs including ChatGPT, GPT-4, embeddings, and more.

---

## 🤖 AI Providers & Runtimes

- **[Ollama](https://ollama.com)**  
  Run open-source large language models (LLMs) locally on your machine with simple commands.

- **[AWS Bedrock](https://aws.amazon.com/bedrock/)**  
  Access foundation models from various providers via a fully managed AWS service.

- **[Docker Desktop](https://www.docker.com/products/docker-desktop/)**  
  Essential for running local AI model runtimes and Docker Compose setups used in the course.

- **[Docker Model Runner](https://docs.docker.com/ai/model-runner/)**  
  Use Docker’s official tool for running and managing AI models locally.

---

## 📚 Foundational Papers & Tools

- **[Attention Is All You Need (Transformer Paper)](https://arxiv.org/abs/1706.03762)**  
  The seminal research paper that introduced the Transformer architecture behind modern LLMs.

- **[OpenAI Tokenizer Tool](https://platform.openai.com/tokenizer)**  
  Visualize how OpenAI tokenizes input prompts and estimate token usage.

---

## 📦 Vector Store & MCP

- **[Qdrant Vector Database](https://qdrant.tech)**  
  An open-source vector store used in Retrieval-Augmented Generation (RAG) demos with Spring AI.

- **[Model Context Protocol (MCP)](https://modelcontextprotocol.io/)**  
  A protocol for connecting AI clients and servers in a decoupled and extensible way.

---

## 📊 Observability & Monitoring Tools

- **[Prometheus](https://prometheus.io/)**  
  Monitoring and alerting toolkit for collecting Spring Boot and AI app metrics.

- **[Micrometer](https://micrometer.io/)**  
  Java metrics collection library used with Spring Boot to expose observability data.

- **[OpenTelemetry](https://opentelemetry.io/)**  
  Industry-standard framework for distributed tracing and telemetry data.

- **[Grafana](https://grafana.com/)**  
  Visualization tool for creating dashboards from Prometheus and other data sources.

- **[Jaeger Tracing](https://www.jaegertracing.io/)**  
  Distributed tracing platform used to trace and monitor AI request flows.

---

## 🚀 How to Run (section01/ollama)

This branch is focused on the first lesson: a minimal Spring Boot app that chats with a local LLM via [Ollama](https://ollama.com).

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
