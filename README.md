# From Java Dev to AI Engineer: Spring AI Fast Track

A personal learning path for **Spring AI**, organized as one branch per lesson. Each branch builds on the previous one and contains a single self-contained Spring Boot project.

> 👉 **You are on the `section0` branch — the baseline.** Plain Spring Boot web service, **no Spring AI**, **no LLM**, **no external infrastructure**. Every later branch layers one capability on top of this same shape.

---

## 🧭 Solution Structure

The repository uses **one branch per lesson**. Each branch contains a single Maven project under `sectionNN/<project>/` so the diff between branches shows exactly what each lesson introduces. Switch lessons with `git checkout <branch>`.

### This branch — `section0`

**Purpose**: the **baseline** every later lesson builds on. A plain Spring Boot 3.5 web service with a single `GET /api/hello` endpoint returning `"Hello, World!"`. **No Spring AI, no LLM, no external infrastructure** — just a working REST controller, a context-load test, and a WebMvc slice test.

Think of it as the empty canvas: when you `git diff section0 section01`, you'll see exactly what adding the first Spring AI capability costs (dependencies, config, code, tests).

### Conventions shared across all branches

- **Build tool**: Maven Wrapper (`./mvnw`) — no global Maven install required.
- **Java**: 21+ (`<java.version>21</java.version>` in every `pom.xml`).
- **Spring Boot**: 3.5.x.
- **Package root**: `com.eazybytes.<project>` (e.g. `com.eazybytes.hello`).
- **REST base path**: `/api` (class-level `@RequestMapping("/api")`), with one endpoint per lesson.
- **Default port**: `8080` (override with `--server.port=<port>` at runtime).
- **Tests**: at minimum a `@SpringBootTest` context-load test plus a `@WebMvcTest` slice test for the controller. Lessons that introduce external services add an `@Tag("e2e")` Testcontainers IT, gated behind an `e2e` Maven profile.

---

## 🚀 How to Run (section0/hello)

A plain Spring Boot 3.5 web service that exposes a single endpoint:

```
GET http://localhost:8080/api/hello   →   200 OK  "Hello, World!"
```

That's the whole app. There is no Spring AI, no LLM, no Docker, no API keys.

### Project layout

```
section0/hello/
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/
└── src/
    ├── main/
    │   ├── java/com/eazybytes/hello/
    │   │   ├── HelloApplication.java          # @SpringBootApplication entry point
    │   │   └── controller/HelloController.java # GET /api/hello
    │   └── resources/application.properties   # app name + log pattern
    └── test/java/com/eazybytes/hello/
        ├── HelloApplicationTests.java          # @SpringBootTest context loads
        └── controller/HelloControllerTest.java # @WebMvcTest slice test
```

### Prerequisites

| Tool | Why | Notes |
|------|-----|-------|
| **JDK 21+** | Spring Boot 3.5 requires Java 21 (set as `<java.version>21</java.version>` in `pom.xml`) | Temurin / Zulu / Amazon Corretto. Check with `java -version`. |
| **Maven Wrapper** | Builds and runs the app | Ships with the project (`./mvnw`) — no global Maven install needed |

> If your default `java` is older than 21 (e.g. JDK 17), point Maven at a 21+ JDK for this shell session — for example: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-25.jdk/Contents/Home`. On macOS you can list installed JDKs with `/usr/libexec/java_home -V`.

### 1. (Optional) Build and run the tests

```bash
cd section0/hello
./mvnw clean verify
```

This compiles the code, runs both tests (~2 s), and produces a runnable JAR at `target/hello-0.0.1-SNAPSHOT.jar`. Skip this step if you just want to start the app.

### 2. Start the app

Pick **one** of the two equivalent ways:

**A. Via the Spring Boot Maven plugin** (best for development — hot reload via `spring-boot-devtools`):

```bash
cd section0/hello
./mvnw spring-boot:run
```

**B. As an executable JAR** (closer to production):

```bash
cd section0/hello
./mvnw clean package -DskipTests
java -jar target/hello-0.0.1-SNAPSHOT.jar
```

Either way, look for this line in the console — that's the signal the app is ready:

```
... INFO  [main] o.s.b.w.embedded.tomcat.TomcatWebServer - Tomcat started on port 8080 (http) ...
... INFO  [main] c.e.h.HelloApplication                   - Started HelloApplication in 0.8 seconds (process running for 1.1)
```

The app listens on `http://localhost:8080`.

To **stop** it, press `Ctrl+C` in the terminal where it's running.

### 3. Call the endpoint

| Method | URL | Query params | Response |
|--------|-----|--------------|----------|
| `GET`  | `http://localhost:8080/api/hello` | none | `text/plain` — `Hello, World!` |

**With curl** (add `-i` to also see headers):

```bash
$ curl http://localhost:8080/api/hello
Hello, World!
```

**With httpie:**

```bash
http :8080/api/hello
```

**In a browser:** open <http://localhost:8080/api/hello>

> Endpoint defined in `section0/hello/src/main/java/com/eazybytes/hello/controller/HelloController.java`. The base path `/api` comes from the class-level `@RequestMapping`; the `/hello` segment from the method-level `@GetMapping`.

### Running just the tests

```bash
cd section0/hello
./mvnw test
```

Runs the WebMvc slice test (`HelloControllerTest`) and the context-load test (`HelloApplicationTests`) — ~2 s, no infrastructure required, safe in CI.

### Troubleshooting

| Symptom | Likely cause / fix |
|---------|--------------------|
| `release version 21 not supported` during `./mvnw` | Maven is using a pre-21 JDK. Set `JAVA_HOME` to a 21+ JDK (see the note under Prerequisites). |
| `Port 8080 was already in use` on startup | Another process is on that port. Either stop it (`lsof -i :8080`) or run on a different port: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090` (or `java -jar target/hello-0.0.1-SNAPSHOT.jar --server.port=9090`). |
| `curl` returns nothing / connection refused | The app hasn't finished starting yet — wait for the `Started HelloApplication` log line. |
| `./mvnw: Permission denied` | Make the wrapper executable: `chmod +x mvnw`. |

### What's next

Switch to `section01` to add the first Spring AI integration on top of this same shape — a `ChatClient`-driven `/api/chat` endpoint backed by a local Ollama model:

```bash
git checkout section01
```

---

## 📎 Stay Connected

---

📬 For questions or issues, raise a GitHub issue or connect with the course instructor

Happy Learning! 🚀  
