# 09 — DevOps & Infrastructure

## 1. Environments

| Environment | Purpose | Update Trigger |
|---|---|---|
| `local` | Developer laptop via docker-compose | Manual |
| `dev` | Integration testing, shared team env | Push to `develop` branch |
| `staging` | Pre-production, UAT, performance testing | Push to `release/*` |
| `production` | Live | Manual approval after staging |

---

## 2. Docker Compose (Local Dev)

```yaml
# docker-compose.yml
version: '3.9'

services:
  postgres-animal:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: farm_animals
      POSTGRES_USER: farm_user
      POSTGRES_PASSWORD: farm_pass
    ports: ["5432:5432"]
    volumes: [postgres_animal_data:/var/lib/postgresql/data]

  postgres-health:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: farm_health
      POSTGRES_USER: farm_user
      POSTGRES_PASSWORD: farm_pass
    ports: ["5433:5432"]

  timescaledb:
    image: timescale/timescaledb:latest-pg16
    environment:
      POSTGRES_DB: farm_milk
      POSTGRES_USER: farm_user
      POSTGRES_PASSWORD: farm_pass
    ports: ["5434:5432"]

  mongodb:
    image: mongo:7
    ports: ["27017:27017"]
    environment:
      MONGO_INITDB_DATABASE: farm_notifications

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    command: redis-server --requirepass farm_redis_pass

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    depends_on: [zookeeper]

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    ports: ["8180:8080"]
    command: start-dev --import-realm
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    volumes: [./keycloak/realm-export.json:/opt/keycloak/data/import/realm.json]

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports: ["8090:8080"]
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092

volumes:
  postgres_animal_data:
```

---

## 3. CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [develop, main, 'release/*']
  pull_request:
    branches: [develop, main]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [
          farm-animal-service,
          farm-health-service,
          farm-breeding-service,
          farm-milk-service,
          farm-inventory-service,
          farm-finance-service,
          farm-notification-service,
          farm-report-service
        ]
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run Tests
        working-directory: ${{ matrix.service }}
        run: mvn verify -Dspring.profiles.active=test

      - name: SonarQube Scan
        run: mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}

      - name: Build Docker Image
        run: |
          docker build -t ghcr.io/${{ github.repository }}/${{ matrix.service }}:${{ github.sha }} \
            ./${{ matrix.service }}

      - name: Push to GHCR
        if: github.ref == 'refs/heads/develop' || startsWith(github.ref, 'refs/heads/release')
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}/${{ matrix.service }}:${{ github.sha }}

  deploy-dev:
    needs: build-and-test
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Dev
        run: |
          kubectl set image deployment/${{ matrix.service }} \
            ${{ matrix.service }}=ghcr.io/${{ github.repository }}/${{ matrix.service }}:${{ github.sha }} \
            --namespace=farm-dev

  deploy-production:
    needs: build-and-test
    if: github.ref == 'refs/heads/main'
    environment: production          # Requires manual approval in GitHub
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Production
        run: kubectl apply -f k8s/production/ --namespace=farm-prod
```

---

## 4. Kubernetes Manifests

### Deployment (Animal Service example)
```yaml
# k8s/deployments/animal-service.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: farm-animal-service
  namespace: farm-prod
spec:
  replicas: 2
  selector:
    matchLabels:
      app: farm-animal-service
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0             # zero-downtime deployment
  template:
    metadata:
      labels:
        app: farm-animal-service
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8081"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
        - name: farm-animal-service
          image: ghcr.io/org/farm-animal-service:latest
          ports:
            - containerPort: 8081
          env:
            - name: DB_HOST
              valueFrom:
                secretKeyRef:
                  name: animal-db-secret
                  key: host
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: animal-db-secret
                  key: password
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 20
            periodSeconds: 5

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: farm-animal-service-hpa
  namespace: farm-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: farm-animal-service
  minReplicas: 2
  maxReplicas: 6
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

---

## 5. Observability Stack

### Prometheus Scrape Config
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'farm-services'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names: [farm-prod]
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
```

### Grafana Dashboards (provision as code)
```
grafana/
├── dashboards/
│   ├── farm-overview.json          # Farm KPIs: animals, milk, alerts
│   ├── service-health.json         # Per-service: latency, error rate, throughput
│   ├── kafka-monitoring.json       # Topic lag, message rates
│   └── jvm-metrics.json            # Heap, GC, threads per service
└── datasources/
    ├── prometheus.yml
    └── loki.yml
```

### Key Grafana Panels
- **Farm Overview:** Total animals (cow vs goat), today's milk production, open alerts count
- **Service Health:** Request rate, p95 latency, error % — one row per service
- **Kafka Health:** Consumer lag per topic, message production rate
- **Business Metrics:** New registrations/week, vaccination compliance %, milk trend

---

## 6. Dockerfile (Spring Boot services)

```dockerfile
# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseZGC \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 7. Infrastructure Runbook Quick Reference

| Scenario | Command |
|---|---|
| Scale animal service | `kubectl scale deployment farm-animal-service --replicas=4 -n farm-prod` |
| Check pod logs | `kubectl logs -f deployment/farm-animal-service -n farm-prod` |
| Force pod restart | `kubectl rollout restart deployment/farm-animal-service -n farm-prod` |
| Check Kafka lag | `kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group health-service` |
| Redis cache flush | `redis-cli -a $PASS FLUSHDB` (use sparingly) |
| DB backup now | `kubectl exec postgres-animal-pod -- pg_dump farm_animals > backup.sql` |
| View all pods status | `kubectl get pods -n farm-prod -w` |
