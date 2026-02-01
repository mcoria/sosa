# SOSA — Scalable Orchestrator for Self‑Play and Analysis (Chess)

SOSA is a distributed chesstango engine orchestrator that integrates with Lichess via the chariot API client.
It leverages Kubernetes for container orchestration and RabbitMQ for asynchronous messaging between components. 
The system coordinates multiple concurrent chesstango engines across a scalable cluster of workers.

## Modules

- model: Shared messages and domain model used across modules.
- worker: Engine worker that plays/moves/analyses positions on demand.
- worker-init: Initialization worker that prepares a game/session before the main worker takes over.
- master: Head node that integrates with Lichess, produces jobs/messages, and coordinates workers.

## Key Technologies

- Kubernetes
- Java, Spring Boot, Spring Batch
- RabbitMQ for messaging
- chesstango (chess engine)


## Architecture Overview

- master subscribes to events and drives the workflow.
  - Integrates with Lichess (challenge bots/users, watch games).
  - Publishes messages to RabbitMQ (exchange: see Constants.CHESS_TANGO_EXCHANGE in code) such as GameStart, StartPosition, GoFast, GameEnd.
- worker-init receives early game setup messages and prepares the session.
- worker performs the heavy lifting (engine interactions, move generation, analysis).
- model defines the shared message classes (e.g., StartPosition, GameStart, GameEnd).

## Prerequisites

- JDK 25+
- Maven 3.9+
- RabbitMQ instance accessible to master and workers
- (Optional) Lichess account and API token if using Lichess integration
- Docker and Kubernetes if deploying via k8s manifests

## Building

Use the Maven wrapper from the repo root:

- On Windows: mvnw.cmd clean install
- On Linux/macOS: ./mvnw clean install

This builds all modules: model, worker, worker-init, master.

## Running Locally

At minimum, master requires RabbitMQ connection details. If using Lichess integration, provide a bot or user token.

Environment variables used by master (see master/src/main/resources/application.yaml):

- BOT_TOKEN: Lichess token used by the bot/user (optional unless using Lichess).
- CHALLENGE_JOB: true|false — Enable periodic challenging of opponents (default false).
- CHALLENGE_TYPES: comma list of time controls (e.g., bullet,blitz,rapid).
- GAME_WATCHDOG: true|false — Enable watchdog to track/expire games (default false).
- RABBIT_HOST: RabbitMQ host.
- RABBIT_USER: RabbitMQ username.
- RABBIT_PASSWORD: RabbitMQ password.

You can also see an example of local configuration under k8s/environments/local/master-configMap.yaml.

Run modules with Spring Boot after building:

- Master: from the master module directory, run mvnw spring-boot:run (or java -jar target/master-*.jar with env vars).
- Worker / worker-init: similarly run via spring-boot:run or the shaded JARs, ensuring they point to the same RabbitMQ.

## Kubernetes Deployment

Kubernetes manifests live under k8s/ with environment overlays:

- k8s/environments/local: example local ConfigMap values (e.g., CHALLENGE_JOB, GAME_WATCHDOG, CHALLENGE_TYPES).
- Additional environment directories (dev, common) can be adapted per cluster.

Typical steps:

1. Prepare a Kubernetes namespace and a RabbitMQ service/secret.
2. Create ConfigMaps and Secrets with required environment variables (e.g., BOT_TOKEN, RABBIT_*).
3. Apply Deployments/Services for master, worker, worker-init (manifests not shown here; customize as needed).

## Development Notes

- Java version configured via pom.xml properties: java.version=21.
- Dependencies managed centrally in the root pom (chesstango, gardel, chariot, commons-collections).
- Spring Boot main application type for master is non-web (batch/daemon).
- Messaging: master publishes to an exchange and uses routing keys based on worker IDs (see GameProducer for examples).

## Testing

Run all tests:

- mvnw.cmd test (Windows) or ./mvnw test (Linux/macOS)

Module-specific tests can be executed by running Maven from the module directory.

## Troubleshooting

- RabbitMQ connection errors: verify RABBIT_HOST, RABBIT_USER, RABBIT_PASSWORD, and that ports are accessible.
- Lichess authentication issues: ensure BOT_TOKEN has proper scopes and the account/bot is enabled.
- No challenges sent: set CHALLENGE_JOB=true and define CHALLENGE_TYPES; ensure the app logs show Lichess connectivity.
- Timeouts/watchdog: GAME_WATCHDOG controls a scheduler; verify timing settings (challengeExpire, gameExpire) in application.yaml.

## License

This project is distributed under the terms of the LICENSE file in this repository.
