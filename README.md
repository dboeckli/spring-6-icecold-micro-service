# Spring 6 Microservice Hwith Kafka Integration

## Abstract

This project implements a modern microservice architecture using Spring 6 and Apache Kafka, deployed on Kubernetes using Helm. The solution features a comprehensive deployment
pipeline with robust monitoring, logging, and testing capabilities. Key components include:

- A Spring 6 microservice with Kafka message broker integration
- Containerized deployment using Kubernetes and Helm
- Structured JSON logging with Logstash encoding
- Automated dependency management through Renovate
- Health monitoring via Spring Actuator endpoints
- Comprehensive test suite for Kafka connectivity and service health
- Time zone aware configuration with container-level customization
- Infrastructure as Code (IaC) approach with Helm charts

The project emphasizes DevOps best practices, including automated testing, health monitoring, and infrastructure automation. It's designed for high availability and maintainability
in cloud-native environments, with particular attention to operational concerns such as logging, monitoring, and dependency management.

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

To run maven filtering for destination target/helm

```bash
mvn clean install -DskipTests 
```

Go to the directory where the tgz file has been created after 'mvn install'

```powershell
cd target/helm/repo
```

unpack

```powershell
$file = Get-ChildItem -Filter spring-6-icecold-micro-service-chart-*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install

```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace spring-6-icecold-micro-service --create-namespace --wait --timeout 8m --debug --render-subchart-notes
```

show logs

```powershell
kubectl get pods -l app.kubernetes.io/name=$APPLICATION_NAME -n spring-6-icecold-micro-service
```

replace $POD with pods from the command above

```powershell
kubectl logs $POD -n spring-6-icecold-micro-service --all-containers
```

test

```powershell
helm test $APPLICATION_NAME --namespace spring-6-icecold-micro-service --logs
```

uninstall

```powershell
helm uninstall $APPLICATION_NAME --namespace spring-6-icecold-micro-service
```

delete all

```powershell
kubectl delete all --all -n spring-6-icecold-micro-service
```

create busybox sidecar

```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=spring-6-icecold-micro-service --command -- sh
```

and analyze kafka connections

```powershell
nslookup spring-6-icecold-micro-service-kafka.spring-6-icecold-micro-service.svc.cluster.local

nc -zv spring-6-icecold-micro-service-kafka.spring-6-icecold-micro-service.svc.cluster.local 29092
echo "Exit code for port 29092: $?"
```

create kafka sidecar

```powershell
kubectl run kafka-test --rm -it --image=bitnamilegacy/kafka:3.9.0 --namespace=spring-6-icecold-micro-service --command -- sh
```

run kafka commands

```powershell
cd /opt/bitnami/kafka/bin
./kafka-topics.sh --bootstrap-server spring-6-icecold-micro-service-kafka.spring-6-icecold-micro-service.svc.cluster.local:29092 --list
```

You can use the actuator rest call to verify via port 30080

## Sandbox (local dev environment)

The sandbox is provisioned by the opencode-sandbox-kit and runs as a Docker container. It mounts this
repo, starts the agent, and connects the IntelliJ MCP server. The app runs on port `8080`; `compose.yaml`
provides Kafka.

Allow the kit source (GitHub without cloning):

```powershell
sbx settings set kit.allowedSources --% "[\"docker.io/\",\"github.com/dboeckli/\"]"
```

Start a new sandbox:

```powershell
sbx run opencode `
    --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" `
    --template docker/sandbox-templates:opencode-docker-0.5.0 `
    --no-share-skills `
    --static-mcp idea `
    . `
    "C:\development\maven-repo:ro"
```

Start the sandbox with Kubernetes support:

```powershell
sbx run opencode `
    --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" `
    --template docker/sandbox-templates:opencode-docker-0.5.0 `
    --no-share-skills `
    --static-mcp idea `
    . `
    "C:\development\maven-repo:ro" `
    "$env:USERPROFILE\.kube:ro"
```

Claude Code (Home) and Mammouth Code variants:

```powershell
sbx run claude `
    --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" `
    --template docker/sandbox-templates:claude-code-docker-0.5.0 `
    --no-share-skills `
    --static-mcp idea `
    . `
    "C:\development\maven-repo:ro"
```

```powershell
sbx run "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=mammouth-agent" `
    --no-share-skills `
    --static-mcp idea `
    . `
    "C:\development\maven-repo:ro"
```

### Start the app

Kafka can be started manually (otherwise it starts with the app):

```shell
docker compose up
```

Then run the `Spring6IcecoldMicroServiceApplication` run configuration in IntelliJ
(`.run/Spring6IcecoldMicroServiceApplication.run.xml`) or start via `./mvnw spring-boot:run`.

### Sandbox build quirk

The sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's `npm install`
(prettier) would fail with `EPERM` unless npm skips bin links. The kit sets `npm_config_bin_links=false`
globally, so no manual export is needed.
