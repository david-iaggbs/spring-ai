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

## 🚀 How to Run (section0/hello)

This branch is the **baseline** — a plain Spring Boot web service with a single `GET /api/hello` endpoint that returns `Hello, World!`. No Spring AI, no Ollama, no external infrastructure. It exists as the starting point you compare every later section against: each subsequent branch (`section01`, `section02`, …) adds one Spring AI capability on top of this same shape.

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (`<java.version>21</java.version>`) | Temurin / Zulu / Oracle |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global install needed |

That's it. No Docker, no Podman, no API keys, no models — everything runs from the JVM.

### 1. Start the Spring Boot app

```bash
cd section0/hello
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 2. Call the endpoint

The app exposes a single endpoint:

| Method | URL | Query params | Response |
|--------|-----|--------------|----------|
| `GET`  | `http://localhost:8080/api/hello` | none | `text/plain` — `Hello, World!` |

**With curl:**

```bash
curl http://localhost:8080/api/hello
```

**With httpie:**

```bash
http :8080/api/hello
```

**In a browser:** open <http://localhost:8080/api/hello>

> Endpoint defined in `section0/hello/src/main/java/com/eazybytes/hello/controller/HelloController.java`. Port `8080` is the Spring Boot default — override with `--server.port=9090` if it conflicts.

### Running the tests

A WebMvc slice test of the controller plus a context-load test. No infrastructure needed.

```bash
cd section0/hello
./mvnw test
```

Runs in ~2 s. Safe in CI as-is.

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀  
