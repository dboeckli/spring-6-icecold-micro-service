# AGENTS.md

Spring Boot 4 (parent 4.1.1) / Spring Framework 6 Kafka microservice on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package
`ch.dboeckli.springframeworkguru.spring6icecoldmicroservice`. It consumes drink requests from Kafka
(`DrinkRequestListener`) and exposes Actuator endpoints on port `8080`.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=DrinkRequestListenerTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop).
- Start locally: `./mvnw spring-boot:run` (app on `:8080`, Kafka via `compose.yaml`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`.
- Spotless flexmark also formats markdown, so this file and any `.md` edits must stay flexmark-clean;
  run `./mvnw spotless:apply` after editing markdown.

## External dependency gotcha

- DTOs (e.g. the drink request payload) come from the external module
  `ch.dboeckli.guru.springframework:spring-6-rest-mvc-api`, resolved from GitHub Packages
  (`maven.pkg.github.com`). Without a PAT in `~/.m2/settings.xml` (server id `github`) the build
  cannot resolve dependencies.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe).
- The listener test (`DrinkRequestListenerTest`) uses `@EmbeddedKafka` + profile `test`.
- ITs (`Spring6IcecoldMicroServiceApplicationIT`, `KafkaConfigIT`, `ActuatorInfoIT`) use
  `@SpringBootTest` + profile `it`; they need Docker (Kafka from `compose.yaml`).

## Architecture

- `listener/DrinkRequestListener` consumes Kafka messages; `services/DrinkRequestProcessor`
  (interface + `*Impl`) processes them.
- `config/KafkaConstants` defines the topics; `health/KafkaHealthIndicator` backs the Actuator health.
- Topics and connection settings are config-driven (`application*.yaml`), not hardcoded.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/`, packaged to `target/helm/repo/`, release name =
  artifactId, namespace `spring-6-icecold-micro-service`.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Dependency updates are managed via `.github/renovate.json`; validate changes with
  `renovate-config-validator`.
