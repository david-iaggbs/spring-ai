# Lesson 9 from Section09 - Phase 3 Execution

Date: 2026-06-25
Scope: section09 only

## Step 3.1 - Narrative bridge (Lesson 8 -> Lesson 9 -> Lesson 10)

- Lesson 8 establishes the base chat flow in Spring AI: prompt handling, controller wiring, and predictable local development behavior.
- Lesson 9 (this section09 scope) expands that foundation into practical AI application patterns already present in the codebase:
  - chat configuration variants (`ChatClientConfig`, memory/time/help-desk/web-search RAG configs)
  - endpoint-focused controllers (`ChatController`, `RAGController`, `HelpDeskController`, `StreamController`)
  - domain workflow with tools and persistence (`HelpDeskTools`, `HelpDeskTicketService`, repository/entity)
  - reliability and observability enablers (`TokenUsageAuditAdvisor`, OpenTelemetry exporter config)
- Lesson 10 should naturally continue from Lesson 9 by hardening for production-like delivery: stricter guardrails, deployment/runtime standardization, repeatable performance checks, and operational runbooks.

## Step 3.2 - Quality gates and local Ollama-on-Podman E2E baseline

### Quality gates (local)

1. Build gate
- Command: `./section09/springai/mvnw -f section09/springai/pom.xml -q -DskipTests compile`
- Pass criteria: exit code 0.

2. Test gate
- Command: `./section09/springai/mvnw -f section09/springai/pom.xml -q test`
- Pass criteria: all tests pass.

3. App startup gate
- Command: `podman compose -f section09/springai/compose.yml up -d`
- Pass criteria: application and supporting services are healthy and reachable.

4. Functional API gate
- Command example: `curl -sS http://localhost:8080/` (replace with lesson9 endpoint contract in use)
- Pass criteria: HTTP success and valid response payload.

5. Observability gate
- Check that token usage audit advisor and telemetry export path are active in logs/config.
- Pass criteria: expected advisor/telemetry signals appear without runtime errors.

### Ollama-on-Podman E2E baseline (minimal)

- Runtime baseline assumptions:
  - Podman is installed and running.
  - Ollama container/service is available through the local compose stack or equivalent local route.
  - Spring AI app resolves the Ollama endpoint from local config.

- Baseline E2E flow:
  1. Start stack with Podman compose from `section09/springai/compose.yml`.
  2. Start or verify the Spring AI app process.
  3. Send one deterministic prompt to a lesson9 endpoint.
  4. Verify response body is non-empty and semantically valid for the endpoint.
  5. Verify no critical errors in logs and advisor telemetry signals are present.

- Baseline evidence to capture per run:
  - command exit codes
  - endpoint response sample
  - timestamped log snippet for advisor/telemetry activity

## Step 3.3 - Artifact consistency checks

Consistency checks for this phase document:
- Must include the bridge terms: Lesson 8, Lesson 9, Lesson 10.
- Must include quality gate definitions.
- Must include Ollama + Podman + E2E baseline language.
- Must remain under `section09` path only.
