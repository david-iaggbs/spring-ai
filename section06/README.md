# Section 06 — AI Tool Calling (Function Calling)

This lesson builds on [Section 05](../section05/) by adding **AI Tool Use** to the Spring AI application: the LLM can now invoke real Java methods at runtime to take actions on behalf of the user.

---

## What you'll learn

- Declaring Spring beans as **AI tools** with `@Tool`, `@ToolParam`, and `ToolContext`
- Controlling how tool results flow back to the user with `returnDirect`
- Wiring tools into a `ChatClient` both statically (`defaultTools`) and per-request (`.tools(...)`)
- Passing request-scoped context (e.g. authenticated username) into tool executions via `ToolContext`
- Persisting tool outputs to a **JPA-backed database** and exposing them through the same tool
- Handling tool execution exceptions with `DefaultToolExecutionExceptionProcessor`

---

## Key Spring AI concepts

| Concept | Where |
|---------|-------|
| `@Tool` / `@ToolParam` | `tools/TimeTools.java`, `tools/HelpDeskTools.java` |
| `ToolContext` | `tools/HelpDeskTools.java` — reads `username` from context |
| `returnDirect = true` | `HelpDeskTools.createTicket` — returns tool result as-is |
| Named `ChatClient` beans | `config/TimeChatClientConfig.java`, `config/HelpDeskChatClientConfig.java` |
| Per-request `.tools(...)` | `controller/HelpDeskController.java` |
| Tool + Memory + RAG combined | All advisors (memory, RAG, token audit) compose with tools |

---

## Prerequisites

- Ollama running locally with **llama3.2:1b** and **nomic-embed-text** pulled:
  ```bash
  ollama pull llama3.2:1b
  ollama pull nomic-embed-text
  ```
- Docker / Podman running (Qdrant is started automatically via Docker Compose)

---

## Running the application

```bash
cd section06/springai
./mvnw spring-boot:run
```

---

## REST endpoints

| Endpoint | Header | Description |
|----------|--------|-------------|
| `GET /api/tools/local-time?message=…` | `username` | Chat client with TimeTools — ask for time in any timezone |
| `GET /api/tools/help-desk?message=…` | `username` | Chat client with HelpDeskTools — create/view support tickets |
| `GET /api/chat?message=…` | — | Basic chat (HR assistant persona, from section02) |
| `GET /api/rag/document/chat?message=…` | `username` | RAG over HR Policy PDF (from section05) |

---

## Test pyramid

| Layer | Class | Command |
|-------|-------|---------|
| Unit | `ToolsControllerTest` | `./mvnw test` |
| Integration | `ToolsControllerIntegrationTest` | `./mvnw test` |
| E2E (Testcontainers) | `ToolsControllerOllamaIT` | `./mvnw test -Pe2e` |

**Fast tests** (unit + integration) run without any external services — AI model and Qdrant are mocked. They complete in a few seconds.

**E2E tests** spin up real `ollama/ollama:latest` and `qdrant/qdrant:latest` containers via Testcontainers. Running them requires Docker / Podman with a forwarded socket:

```bash
# Forward Podman socket (get <port> from: docker context ls)
ssh -i ~/.local/share/containers/podman/machine/machine -p <port> \
  -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile=/dev/null \
  -fN -L /tmp/tc-podman.sock:/run/user/501/podman/podman.sock core@127.0.0.1

export DOCKER_HOST=unix:///tmp/tc-podman.sock TESTCONTAINERS_RYUK_DISABLED=true
JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-26.jdk/Contents/Home \
  ./mvnw test -Pe2e
```

---

## Project structure (new in this section)

```
tools/
  TimeTools.java              — @Tool: getCurrentLocalTime(), getCurrentTime(timezone)
  HelpDeskTools.java          — @Tool: createTicket(returnDirect), getTicketStatus()
config/
  TimeChatClientConfig.java   — "timeChatClient" bean with TimeTools
  HelpDeskChatClientConfig.java — "helpDeskChatClient" bean with TimeTools + memory
controller/
  TimeController.java         — GET /api/tools/local-time
  HelpDeskController.java     — GET /api/tools/help-desk
entity/
  HelpDeskTicket.java         — JPA entity (id, username, issue, status, createdAt, eta)
service/
  HelpDeskTicketService.java  — createTicket(), getTicketsByUsername()
repository/
  HelpDeskTicketRepository.java
```
