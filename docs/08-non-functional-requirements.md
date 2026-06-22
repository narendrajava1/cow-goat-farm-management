# 08 — Non-Functional Requirements

## 1. Performance

| Metric | Target | Measurement |
|---|---|---|
| API response time (p95) | < 300ms | Prometheus histogram |
| API response time (p99) | < 1000ms | Prometheus histogram |
| Milk bulk entry (30 animals) | < 2s | End-to-end |
| Dashboard load time | < 1.5s | Lighthouse |
| Report PDF generation | < 5s | Service metric |
| Kafka event processing lag | < 5s | Kafka consumer lag |
| Notification delivery (email) | < 60s | SendGrid webhook |

---

## 2. Scalability

- **Horizontal scaling:** All services stateless, deployable as multiple pods
- **Database:** Read replicas for Report Service queries
- **Kafka:** Partitioned by `farmId` — scales linearly with number of farms
- **Redis:** Cluster mode for high availability
- **Target load:** 100 concurrent users, 500 farms, 50,000 animals

---

## 3. Availability

| Environment | SLA Target |
|---|---|
| Production | 99.5% uptime (< 3.6 hrs downtime/month) |
| Staging | 95% (for testing) |

**Strategy:**
- Kubernetes rolling deployments (zero downtime)
- Pod Disruption Budgets (min 1 pod always running)
- PostgreSQL with streaming replication + automated failover
- Redis Sentinel for cache HA
- Kafka multi-broker cluster (3 brokers minimum)
- Health checks: liveness + readiness probes on all pods

---

## 4. Security

### Authentication & Authorization
- OAuth2 + JWT via Keycloak
- Token expiry: 15 min access token, 7 days refresh token
- Role-based access enforced at API Gateway AND at service level

### Role Permission Matrix
| Feature | OWNER | WORKER | VET | ACCOUNTANT |
|---|---|---|---|---|
| View animal profiles | ✅ | ✅ | ✅ | ✅ |
| Register/edit animals | ✅ | ✅ | ❌ | ❌ |
| Delete/sell animal | ✅ | ❌ | ❌ | ❌ |
| Add health records | ✅ | ✅ | ✅ | ❌ |
| Add vaccinations | ✅ | ✅ | ✅ | ❌ |
| Record milk | ✅ | ✅ | ❌ | ❌ |
| View milk analytics | ✅ | ✅ | ❌ | ✅ |
| Record breeding | ✅ | ✅ | ✅ | ❌ |
| View financials | ✅ | ❌ | ❌ | ✅ |
| Add financial records | ✅ | ❌ | ❌ | ✅ |
| Manage users | ✅ | ❌ | ❌ | ❌ |
| Export reports | ✅ | ❌ | ✅ | ✅ |

### Data Security
- All data encrypted at rest (AES-256 at DB level)
- All transport encrypted (TLS 1.3)
- PII (owner name, contact) masked in logs
- Database credentials via Kubernetes Secrets (not config files)
- No raw SQL — all queries via JPA/Spring Data
- Input validation on all endpoints (`@Valid` + custom validators)
- File uploads: virus scanned, type-validated, max 10MB

### Audit Trail
- All write operations logged with `userId`, `farmId`, `timestamp`, `action`
- Financial transactions immutable (reversal pattern)
- Audit log table in each service DB

---

## 5. Data Retention & Compliance

| Data Type | Retention Period | Reason |
|---|---|---|
| Animal records | 7 years after animal death/sale | Regulatory |
| Health records | 7 years | Veterinary compliance |
| Financial transactions | 7 years | Tax compliance |
| Milk production data | 5 years | Operational |
| Notification history | 6 months | Operational |
| Audit logs | 3 years | Security |

**Implementation:**
- Soft delete (status = ARCHIVED) — no hard deletes
- TimescaleDB data retention policies for milk data
- Monthly database backups retained for 7 years (cold storage)

---

## 6. Observability

### Metrics (Prometheus + Grafana)
- JVM metrics (heap, GC, threads)
- HTTP request rate, latency, error rate (per endpoint)
- Kafka consumer lag
- Database connection pool utilization
- Cache hit/miss ratio
- Business metrics: new animals/day, milk volume/day, alerts sent/day

### Logging (Loki)
- Structured JSON logs (all services)
- Log levels: ERROR for exceptions, WARN for business rule violations, INFO for key events
- `traceId` in every log line (OpenTelemetry propagation)
- No PII in logs

### Tracing (Jaeger + OpenTelemetry)
- Distributed traces across all service calls
- Kafka message trace propagation via headers
- P95 latency per service highlighted in Grafana

### Alerting (Grafana Alerts)
| Alert | Threshold | Channel |
|---|---|---|
| Service error rate | > 5% for 5 min | PagerDuty |
| API p99 latency | > 2s | Slack |
| Kafka consumer lag | > 1000 msgs | Slack |
| DB connection pool > 80% | 5 min | Slack |
| Pod restart count | > 3 in 10 min | PagerDuty |
| Disk usage | > 85% | Slack |

---

## 7. Reliability Patterns

| Pattern | Library | Applied To |
|---|---|---|
| Circuit Breaker | Resilience4j | All inter-service HTTP calls |
| Retry | Resilience4j | Kafka publisher, external APIs (SendGrid, Twilio) |
| Bulkhead | Resilience4j | Isolate slow services from critical path |
| Idempotency | Custom (`X-Idempotency-Key` header) | POST endpoints (prevent duplicate registrations) |
| Dead Letter Queue | Kafka DLQ | Failed Kafka message processing |
| Saga Pattern | Choreography via events | Breeding birth → Animal registration (multi-service transaction) |

---

## 8. Backup & Recovery

| Component | Backup Frequency | RTO | RPO |
|---|---|---|---|
| PostgreSQL | Continuous WAL + daily snapshot | 1 hour | 5 minutes |
| TimescaleDB (milk) | Daily snapshot | 2 hours | 1 hour |
| MongoDB (notifications) | Daily snapshot | 4 hours | 24 hours |
| MinIO/S3 (files) | Cross-region replication | 1 hour | near-zero |
| Kafka | 3-broker replication (RF=3) | minutes | zero |

**Disaster Recovery:**
- Documented runbook for each failure scenario
- Quarterly DR drill
- Target environment (staging) used for DR testing
