# AGENTS.md

This document provides project context, architecture guidelines, build/test commands, and conventions for AI coding agents working on the **SOSA** (Scalable Orchestrator for Self-Play and Analysis) repository.

---

## 1. Project Overview

**SOSA** is a distributed chess engine orchestrator built in Java that coordinates Chesstango chess engine workers, integrates with Lichess via the Chariot API client, communicates asynchronously using RabbitMQ, and scales using Kubernetes.

### Core Tech Stack
- **Java**: 25 (Java compiler release 25)
- **Frameworks**: Spring Boot 4.1.x (Spring Web, Actuator, Batch, AMQP), Quartz Scheduler
- **Resilience**: Resilience4j (RateLimiter)
- **Chess Libraries**:
  - `chesstango`: Chess engine and board logic (`net.chesstango:engine`, `net.chesstango:board`)
  - `chariot`: Lichess API client (`io.github.tors42:chariot`)
  - `gardel`: Chess representations (FEN, EPD, PGN, SAN, LAN) (`net.chesstango:gardel`)
- **Utilities**: Lombok, Apache Commons Collections 4, Jackson
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Rabbit Test
- **Build Tool**: Maven 3.9+ with Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Infrastructure**: Kubernetes manifests, RabbitMQ, Redis

---

## 2. Repository & Module Architecture

The repository is structured as a multi-module Maven project:

```
sosa/
├── model/        # Shared message DTOs, events, and AMQP routing constants
├── master/       # Head node coordinating workers, scheduling bot challenges on Lichess
├── worker/       # Engine worker executing chess moves, analysis, Polyglot books & Syzygy tablebases
├── worker-init/  # Pre-game initialization worker (Spring Batch) that sets up game sessions
├── k8s/          # Kubernetes manifests, PVCs/PVs, ConfigMaps, and environment overlays
└── pom.xml       # Root parent POM managing versions, compiler plugins, and dependencies
```

### Module Responsibilities & Flow
1. **`model` (`sosa-model`)**:
   - Contains immutable message payloads exchanged over RabbitMQ (`sosa.exchange`).
   - Core messages: `GameStart`, `SendMove`, `WorkerBusy`, `WorkerReady`, `Constants`.
   - Must remain lightweight with minimal external dependencies (Lombok only).
2. **`master` (`sosa-master`)**:
   - Interacts with Lichess API (handling challenges, bot queue, game streams).
   - Publishes jobs and coordinates worker lifecycle via RabbitMQ.
   - Uses Quartz Scheduler for periodic challenge creation and watchdog jobs (`DynamicScheduler`, `ExpireChallengesJob`, `ExpireWorkersJob`, `QueryScheduler`).
   - Exposes REST endpoints (`StatusController` at `/status`) to monitor orchestrator status (`SosaStatus`) and scheduler state (`ScheduleStatus`).
3. **`worker` (`sosa-worker`)**:
   - Instantiates and controls the `chesstango` engine (`TangoController`).
   - Evaluates positions, queries opening books (`polyglot_file`) and endgame tablebases (`syzygy_path`).
   - Streams moves and game events with Lichess and master.
4. **`worker-init` (`sosa-worker-init`)**:
   - A Spring Batch job preparing worker state and game metadata before handing off to the worker.
5. **`k8s/`**:
   - Kubernetes deployments and overlays (`k8s/environments/local`, `dev`, `common`).

---

## 3. Build & Test Commands

Use the Maven Wrapper located in the repository root.

### Build Commands
- **Full build (compile, test, package)**:
  - Windows: `.\mvnw.cmd clean install`
  - Linux/macOS: `./mvnw clean install`
- **Fast compile without running tests**:
  - Windows: `.\mvnw.cmd test-compile`
  - Linux/macOS: `./mvnw test-compile`
- **Build a specific module**:
  - Windows: `.\mvnw.cmd clean install -pl master -am`
  - Linux/macOS: `./mvnw clean install -pl master -am`

### Test Commands
- **Run all tests across all modules**:
  - Windows: `.\mvnw.cmd test`
  - Linux/macOS: `./mvnw test`
- **Run tests for a single module**:
  - Windows: `.\mvnw.cmd test -pl master`
  - Linux/macOS: `./mvnw test -pl master`
- **Run a single test class**:
  - Windows: `.\mvnw.cmd test -Dtest=DynamicSchedulerTest`
  - Linux/macOS: `./mvnw test -Dtest=DynamicSchedulerTest`
- **Run a single test method**:
  - Windows: `.\mvnw.cmd test -Dtest=DynamicSchedulerTest#testMethodName`
  - Linux/macOS: `./mvnw test -Dtest=DynamicSchedulerTest#testMethodName`

### Running Applications Locally
- **Master**: `.\mvnw.cmd -pl master spring-boot:run` (or `./mvnw -pl master spring-boot:run`)
- **Worker**: `.\mvnw.cmd -pl worker spring-boot:run` (or `./mvnw -pl worker spring-boot:run`)
- **Worker Init**: `.\mvnw.cmd -pl worker-init spring-boot:run` (or `./mvnw -pl worker-init spring-boot:run`)

---

## 4. Environment Variables & Configuration

Applications are configured via `application.yaml` with environment variable overrides (`master` runs as a servlet web application with `spring.main.web-application-type=servlet` exposing REST APIs, while `worker` and `worker-init` run as non-web services with `spring.main.web-application-type=none`):

| Variable | Target Module(s) | Description | Default / Example |
|---|---|---|---|
| `BOT_TOKEN` | `master`, `worker` | Lichess personal API token | *(Required for Lichess integration)* |
| `RABBIT_HOST` | All | RabbitMQ broker host | `localhost` |
| `RABBIT_USER` | All | RabbitMQ username | `guest` |
| `RABBIT_PASSWORD` | All | RabbitMQ password | `guest` |
| `CHALLENGE_JOB` | `master` | Enable/disable automatic bot challenging job | `true` |
| `CHALLENGE_TIME` | `master` | Interval (in minutes) for bot challenge job | `1` |
| `CHALLENGE_TYPES` | `master` | Comma-separated list of challenge time controls | `bullet,blitz,rapid` |
| `CHALLENGE_EXPIRE` | `master` | Timeout (in minutes) for pending challenges | `2` |
| `WORKER_EXPIRE` | `master` | Timeout (in minutes) for inactive workers | `3` |
| `TOO_MANY_REQUEST_EXPIRE` | `master` | Backoff period for 429 Too Many Requests | `1` |
| `WORKER_ID` | `worker-init` | Identifier for the worker instance | `sosa-worker` |
| `GAME_ABORT` | `worker` | Enable/disable auto-abort for stale games | `true` |
| `GAME_ABORT_TIMER` | `worker` | Timer (in minutes) to abort game if no response | `2` |
| `POLYGLOT_FILE` | `worker` | File path to opening book (`.bin`) | *(Optional)* |
| `SYZYGY_PATH` | `worker` | Directory path to Syzygy endgame tablebases | *(Optional)* |

---

## 5. Coding Conventions & Best Practices

- **Java Version**: Maintain compatibility with Java 25. Avoid features from higher versions without updating `pom.xml`.
- **Lombok**: Use Lombok annotations (`@Getter`, `@Setter`, `@Slf4j`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) to avoid boilerplate. Ensure annotation processors are properly configured in `maven-compiler-plugin`.
- **Logging**: Use SLF4J (`@Slf4j`) for logging. Prefer structured, informative log messages (`log.info(...)`, `log.debug(...)`, `log.error(...)`).
- **Spring Architecture**:
  - Keep configuration classes organized under `net.chesstango.sosa.<module>.configs`.
  - Keep REST controllers organized under `net.chesstango.sosa.<module>.rest`.
  - Keep scheduler services and handlers under `net.chesstango.sosa.<module>.scheduler`.
  - Keep jobs and scheduled tasks organized under `net.chesstango.sosa.<module>.jobs`.
  - Keep AMQP producers and consumers under `queues` or root module packages.
  - Keep domain messages in `net.chesstango.sosa.messages` inside the `model` module.
- **Dependency Hierarchy**:
  - Do NOT introduce cyclic dependencies or violate module boundaries (`model` must never depend on other project modules).
  - Manage all library versions centrally in root `pom.xml` (`<dependencyManagement>`).
- **Code Style**:
  - Indentation: 4 spaces.
  - Follow standard Java naming conventions (PascalCase for classes, camelCase for methods and variables, UPPER_SNAKE_CASE for constants).
  - Add Javadoc with `@author` annotations consistent with existing files.
- **Testing**:
  - Write unit tests for new services, handlers, scheduler jobs, and error parsers using JUnit 5 and Mockito.
  - Avoid modifying or bypassing existing tests unless intentionally changing the behavior.

---

## 6. Prohibitions & Non-Goals

- **Do NOT** commit sensitive secrets, tokens, or credentials into repository files.
- **Do NOT** commit local IDE folders (`.idea/`, `.vscode/`) or build artifacts (`target/`).
- **Do NOT** change Spring Boot or Java compiler version unless explicitly requested.
- **Do NOT** delete or disable existing unit tests to force a build to pass.
