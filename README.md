# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section5` branch.** *RAG*: a `ChatClient` only knows what the model was trained on. This branch makes the assistant **answer from your own documents and from the live web**: `HRPolicyLoader` ingests a PDF into a Qdrant vector store at startup; `RAGController` exposes three endpoints that show the RAG journey — manual similarity search → Spring AI's `RetrievalAugmentationAdvisor` → live web retrieval via a custom `WebSearchDocumentRetriever` backed by the Tavily API. A `PIIMaskingDocumentPostProcessor` redacts emails and phone numbers from retrieved chunks before they reach the model. Built on top of [`section4`](https://github.com/david-iaggbs/spring-ai/tree/section4) and still running entirely on a **local LLM via [Ollama](https://ollama.com)** (`llama3.2:1b` for chat, `nomic-embed-text` for embeddings).

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### This branch — `section5`

**Purpose**: introduce **Retrieval-Augmented Generation (RAG)**. By default the model answers from its training data alone — it knows nothing about your company policies, your latest docs, or today's news. This branch shows how to bridge that gap: load external knowledge into a vector store, retrieve the most relevant chunks at query time, and inject them into the prompt so the model answers from *your* data.

**What it adds on top of the [`section4`](https://github.com/david-iaggbs/spring-ai/tree/section4) baseline** — a fresh `section05/springai` module (dropping `section04/springai`):

- **`rag/HRPolicyLoader.java`** — `@PostConstruct` component that reads `Eazybytes_HR_Policies.pdf` with `TikaDocumentReader`, splits it into 200-token chunks with `TokenTextSplitter`, and stores them in Qdrant at startup. Runs once per app start; the vector store persists across restarts.

- **`rag/WebSearchDocumentRetriever.java`** — a custom `DocumentRetriever` that calls the Tavily Search API, maps each result into a Spring AI `Document` (with `title`, `url`, and relevance `score` metadata), and returns the top-N hits. Reads `TAVILY_SEARCH_API_KEY` from the environment.

- **`rag/PIIMaskingDocumentPostProcessor.java`** — a `DocumentPostProcessor` that runs regex patterns over every retrieved chunk and replaces emails with `[REDACTED_EMAIL]` and phone numbers with `[REDACTED_PHONE]` before the chunks reach the model prompt.

- **`rag/RandomDataLoader.java`** — a commented-out (`// @Component`) loader that seeds random sentences into Qdrant; left in the codebase for instructional reference. Uncomment `@Component` to activate it.

- **`config/WebSearchRAGChatClientConfig.java`** — builds a second named `ChatClient` bean (`webSearchRAGChatClient`) that stacks `SimpleLoggerAdvisor` + `MessageChatMemoryAdvisor` + `TokenUsageAuditAdvisor` + `RetrievalAugmentationAdvisor`. The `RetrievalAugmentationAdvisor` wires in the custom `WebSearchDocumentRetriever` so every prompt is automatically enriched with live web results.

- **`config/ChatClientConfig.java`** — base `ChatClient` bean with `llama3.2:1b`, `TokenUsageAuditAdvisor`, and a default HR-assistant system prompt.

- **`controller/RAGController.java`** — three endpoints under `/api/rag`:

  | Endpoint | `ChatClient` | What it demonstrates |
  |----------|-------------|----------------------|
  | `GET /api/rag/random/chat` | `chatMemoryChatClient` | Memory-only chat (manual vector search shown but commented out — compare approaches in the source) |
  | `GET /api/rag/document/chat` | `chatMemoryChatClient` | Memory-only chat with the HR system prompt (manual similarity search commented out — activate to see document-grounded answers) |
  | `GET /api/rag/web-search/chat` | `webSearchRAGChatClient` | Live web retrieval via `RetrievalAugmentationAdvisor` + `WebSearchDocumentRetriever` |

  All three endpoints accept `username` **header** (→ `CONVERSATION_ID`) and `message` query param.

- **`compose.yml`** — Qdrant service (`qdrant/qdrant:latest`, ports 6333/6334). Spring Boot's Docker Compose integration starts it automatically when you run the app — no separate `docker-compose up` needed.

- **New dependencies**: `spring-ai-rag`, `spring-ai-advisors-vector-store`, `spring-ai-starter-vector-store-qdrant`, `spring-ai-tika-document-reader`, `spring-boot-docker-compose`.

Run `git diff section4 section5` to see exactly what this lesson costs in code, config and dependencies.

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.springai`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **LLM runtime**: all branches run entirely on **[Ollama](https://ollama.com)** — no API keys needed for the model. From section5 onward, a second Ollama model (`nomic-embed-text`) is also required for embeddings.

---

## 🚀 How to Run (section05/springai)

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (`<java.version>21</java.version>`) | Temurin / Zulu / Oracle |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global install needed |
| **Podman** (or Docker) | Runs Ollama + starts Qdrant via `compose.yml` | Tested with Podman; Docker works with the same commands |
| **Ollama runtime** | Chat (`llama3.2:1b`) and embeddings (`nomic-embed-text`) | Run as a container (see step 1) |
| **`TAVILY_SEARCH_API_KEY`** | `/api/rag/web-search/chat` endpoint only | Free tier at [tavily.com](https://tavily.com); skip if you only use the document chat endpoints |

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

# pull the chat model
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
cd section05/springai
./mvnw spring-boot:run
```

Spring Boot detects `compose.yml` and **starts Qdrant automatically** before the app context is created. On first run, `HRPolicyLoader` reads `Eazybytes_HR_Policies.pdf`, splits it into chunks, embeds them with `nomic-embed-text` via Ollama, and stores them in Qdrant — this takes a few seconds. Subsequent runs skip the re-ingestion (Qdrant persists its data inside the container volume while it's running; restart the container to reset it).

The app starts on `http://localhost:8080`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:~/chatmemory`, user: `madan`, password: `12345`).

> **Podman users:** point Spring Boot at the Podman socket before running:
> ```bash
> podman machine start
> export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
> ./mvnw spring-boot:run
> ```

### 3. Query the RAG endpoints

The app exposes three new endpoints under `/api/rag`, all accepting `username` header and `message` query param:

| Method | URL | What happens |
|--------|-----|--------------|
| `GET` | `/api/rag/random/chat` | Memory-backed chat; manual vector search commented out (see source for the pattern) |
| `GET` | `/api/rag/document/chat` | Memory-backed chat with HR system prompt; uncomment similarity search in `RAGController` to ground answers in the PDF |
| `GET` | `/api/rag/web-search/chat` | **Full RAG**: `RetrievalAugmentationAdvisor` retrieves live web results via Tavily and injects them into the prompt automatically |

**With curl:**

```bash
# Ask about the HR policy (activate manual search in RAGController to see document-grounded answers)
curl -H "username: madan03" \
  "http://localhost:8080/api/rag/document/chat?message=What%20is%20the%20leave%20policy%3F"

# Ask a live-web question — Tavily results are injected automatically into the prompt
curl -H "username: madan03" \
  "http://localhost:8080/api/rag/web-search/chat?message=What%20is%20the%20latest%20Java%20LTS%20version%3F"
```

> **Seeing RAG in action:** advisors log at DEBUG (`logging.level.org.springframework.ai.chat.client.advisor=DEBUG` in `application.properties`), so you can watch retrieved document chunks being stitched into each prompt.

> **Web-search endpoint:** set `TAVILY_SEARCH_API_KEY` in your environment before starting the app. The other two endpoints work without it.

### Configuration

All settings live in `section05/springai/src/main/resources/application.properties`:

```properties
# Model
spring.ai.model.chat=ollama
spring.ai.model.embedding=ollama
spring.ai.ollama.chat.options.model=llama3.2:1b
spring.ai.ollama.embedding.options.model=nomic-embed-text

# Conversation memory: file-based H2 so history survives restarts
spring.datasource.url=jdbc:h2:file:~/chatmemory;AUTO_SERVER=true
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
spring.ai.chat.memory.repository.jdbc.schema=classpath:/schema/schema-h2db.sql

# Qdrant vector store (started automatically via compose.yml)
spring.ai.vectorstore.qdrant.initialize-schema=true
spring.ai.vectorstore.qdrant.host=localhost
spring.ai.vectorstore.qdrant.port=6334
spring.ai.vectorstore.qdrant.collection-name=eazybytes
```

Swap `llama3.2:1b` for any other chat model you have pulled in Ollama (e.g. `llama3.2:3b`, `mistral`). Swap `nomic-embed-text` for any Ollama embedding model — just make sure it's pulled first.

### Running the tests

```bash
cd section05/springai
./mvnw test
```

Requires Ollama running at `http://localhost:11434` (with both `llama3.2:1b` and `nomic-embed-text` pulled) and Podman/Docker running so Spring Boot can start Qdrant via `compose.yml`.

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀
