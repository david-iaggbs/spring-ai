# Lesson 9 from Section09 - Phase 3 Execution

Date: 2026-06-26
Scope: section09 only

## Step 3.1 - Runtime strategy decision

Strategy selected: keep [section09/springai/compose.yml](section09/springai/compose.yml) unchanged and require an external Ollama runtime.

Reasoning:
- Lowest-risk path for this branch: no compose topology change.
- Aligns with [section09/springai/src/main/resources/application.properties](section09/springai/src/main/resources/application.properties), which already targets `spring.ai.ollama.base-url=http://localhost:11434`.
- Keeps existing observability/vector store services in compose focused and stable.

External Ollama prerequisite (mandatory):
1. Install and start Ollama locally.
2. Ensure the API is reachable:
```bash
curl -fsS http://localhost:11434/api/tags
```
3. Pull the model used by this section:
```bash
ollama pull llama3.2:1b
```
4. Verify model availability:
```bash
ollama list | grep "llama3.2:1b"
```

## Step 3.2 - Ollama-first setup and smoke flow

From repo root:

1. Start supporting local infra:
```bash
podman compose -f section09/springai/compose.yml up -d
```

2. Compile and run tests quickly:
```bash
./section09/springai/mvnw -f section09/springai/pom.xml -q -DskipTests compile
./section09/springai/mvnw -f section09/springai/pom.xml -q test -DskipITs
```

3. Start the Spring AI app:
```bash
cd section09/springai
./mvnw -q spring-boot:run
```

4. Smoke test chat endpoint:
```bash
curl -fsS -o /tmp/chat.out "http://localhost:8080/api/chat?message=hello"
cat /tmp/chat.out
```

5. Smoke test prompt stuffing endpoint:
```bash
curl -fsS -o /tmp/prompt-stuffing.out "http://localhost:8080/api/prompt-stuffing?message=What%20is%20the%20leave%20policy%3F"
cat /tmp/prompt-stuffing.out
```

Pass criteria:
- `curl http://localhost:11434/api/tags` returns HTTP 200.
- Both endpoint calls return HTTP 200 and non-empty payloads.
- No startup/runtime errors in the Spring app logs.

## Step 3.3 - Command and document consistency checks

Checks to run when updating this phase document:

1. Verify Ollama model in docs matches app config:
```bash
grep -n "llama3.2:1b" section09/docs/lesson9-phase3-execution.md
grep -n "spring.ai.ollama.chat.options.model" section09/springai/src/main/resources/application.properties
```

2. Verify docs explicitly state external Ollama prerequisite:
```bash
grep -n "external Ollama runtime" section09/docs/lesson9-phase3-execution.md
```

3. Verify compose scope remains supporting services (no app/ollama assumptions):
```bash
grep -n "services:" section09/springai/compose.yml
```

## Narrative bridge (Lesson 8 -> Lesson 9 -> Lesson 10)

- Lesson 8 establishes core chat flow and local execution.
- Lesson 9 applies those foundations to multi-controller/tool/advisor patterns with Ollama as chat provider.
- Lesson 10 can build on this by hardening deployment/runtime operations and production guardrails.
