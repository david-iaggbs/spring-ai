# From Java Dev to AI Engineer: Spring AI Fast Track

## 🌱 Spring AI Course – Resources & Reference Links

Welcome to the official GitHub repository for the **Spring AI Course**. This course helps you build intelligent applications using the Spring AI framework and integrate powerful LLMs like OpenAI into your Spring Boot apps.

Below are some carefully curated reference links and tools used throughout the course. Bookmark this information for quick access during development and exploration.

## Branch Lesson Narrative: section9

Purpose:
Move from response-quality focus to an integrated Spring AI application by combining chat, memory, tools, RAG patterns, and observability in the `section09/springai` module.

Bridge framing from section08 to section09:
In section08, the focus is evaluating and improving response reliability. In this section9 lesson, those capabilities are expanded into a more complete application architecture: chat-memory persistence, tool-assisted flows, document/web retrieval, and runtime observability with Prometheus, Grafana, and Jaeger.

Prerequisites:

- JDK 21 available in your terminal (`java -version` should report 21).
- Maven Wrapper executable available in the module folder.
- A valid `OPENAI_API_KEY` exported in your environment.
- Podman or Docker Compose available for local support services.
- Previous lesson reference branch: `section8`.
- Current lesson branch: `section9`.

Explicit lesson delta from previous branch:

```bash
git diff section8 section9
```

Run steps (`section09/springai`):

```bash
cd section09/springai
podman compose up -d
./mvnw spring-boot:run
```

Try the endpoints:

```bash
# Baseline chat
curl "http://localhost:8080/api/chat?message=How many paid leave days do I get?"

# Chat memory flow
curl "http://localhost:8080/api/chat-memory?message=My employee id is 1234"

# Prompt stuffing
curl "http://localhost:8080/api/prompt-stuffing?message=How many paid leave days do I get?"

# RAG with random collection lookup
curl "http://localhost:8080/api/rag/random/chat?message=Tell me about cloud costs"

# RAG with web-search backed retrieval
curl "http://localhost:8080/api/rag/web-search/chat?message=Latest Java LTS version"

# Helpdesk tool flow
curl "http://localhost:8080/api/tools/help-desk?query=I need laptop replacement"
```

Verification steps (`section09/springai`):

```bash
cd section09/springai
./mvnw -q -DskipTests compile
./mvnw test
curl -fsS http://localhost:8080/actuator/prometheus
curl -fsS http://localhost:16686/api/services
```

Expected verification outcome:

- `SpringaiApplicationTests` passes when local dependencies are available.
- The application serves chat, memory, tools, and RAG endpoints from one module.
- Observability pipeline is reachable locally: metrics via `/actuator/prometheus` and traces visible in Jaeger.

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

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀  
