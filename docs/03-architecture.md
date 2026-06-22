# 03 — System Architecture

## 1. Architecture Style

**Microservices** with an **API Gateway** pattern, deployed on **Kubernetes**.  
Each domain service is independently deployable, owns its own database, and communicates via REST (sync) and Kafka (async events).

---

## 2. Technology Stack

### Backend
| Layer | Technology | Reason |
|---|---|---|
| Language | Java 21 | LTS, virtual threads (Project Loom) for high concurrency |
| Framework | Spring Boot 3.3 | Industry standard, rich ecosystem |
| API Gateway | Spring Cloud Gateway | Centralized routing, auth filter, rate limiting |
| Service Discovery | Kubernetes Service DNS | Avoids Eureka overhead in k8s |
| Messaging | Apache Kafka | Durable, high-throughput async events |
| Cache | Redis | Session store, frequently read data (animal profiles) |
| Auth | Keycloak (OAuth2 + JWT) | Enterprise-grade RBAC |

### Database (per service)
| Service | Database | Reason |
|---|---|---|
| Animal, Health, Breeding | PostgreSQL | Relational, complex queries, ACID |
| Milk Production | TimescaleDB (PostgreSQL ext.) | Time-series data, efficient range queries |
| Notifications | MongoDB | Flexible schema for notification payloads |
| Feed & Inventory | PostgreSQL | Relational with strong consistency needs |
| Financial | PostgreSQL | ACID critical for financial records |

### Frontend
| Layer | Technology |
|---|---|
| Web | React 18 + TypeScript + TailwindCSS |
| Mobile | React Native (iOS & Android) |
| State Management | Zustand |
| Charts/Analytics | Recharts |
| Offline Sync | WatermelonDB (mobile) |

### Infrastructure
| Component | Technology |
|---|---|
| Container | Docker |
| Orchestration | Kubernetes (EKS / GKE / on-prem) |
| CI/CD | GitHub Actions |
| Observability | Prometheus + Grafana + Loki |
| Tracing | OpenTelemetry + Jaeger |
| Object Storage | MinIO (self-hosted) / AWS S3 |

---

## 3. Microservices Decomposition

```
┌──────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY :8080                            │
│     /auth/**  /animals/**  /health/**  /breeding/**  /milk/**        │
│     /inventory/**  /finance/**  /notifications/**  /reports/**       │
└──────┬──────────┬──────────┬──────────┬──────────┬──────────────────┘
       │          │          │          │          │
  ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐ ┌──▼─────────┐
  │Animal  │ │Health  │ │Breeding│ │Milk    │ │Inventory   │
  │Service │ │Service │ │Service │ │Service │ │Service     │
  │:8081   │ │:8082   │ │:8083   │ │:8084   │ │:8085       │
  └────┬───┘ └───┬────┘ └───┬────┘ └───┬────┘ └──┬─────────┘
       │         │          │          │          │
  ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐ ┌──▼─────────┐
  │Postgres│ │Postgres│ │Postgres│ │Timescl │ │Postgres    │
  └────────┘ └────────┘ └────────┘ └────────┘ └────────────┘

  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │Finance       │  │Notification  │  │Report        │
  │Service :8086 │  │Service :8087 │  │Service :8088 │
  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
         │                 │                  │
    ┌────▼────┐      ┌─────▼────┐      ┌──────▼───┐
    │Postgres │      │MongoDB   │      │Postgres  │
    └─────────┘      └──────────┘      │+ Redis   │
                                       └──────────┘

                ┌─────────────────────────┐
                │   Apache Kafka          │
                │   Topics:               │
                │   - animal.events       │
                │   - health.events       │
                │   - breeding.events     │
                │   - inventory.alerts    │
                │   - notification.send   │
                └─────────────────────────┘
```

---

## 4. Service Responsibilities

### 4.1 Animal Service (Port 8081)
- CRUD for Animal entity
- Herd management
- Animal status transitions (ACTIVE → SOLD / DECEASED)
- Tag number validation & uniqueness
- Publishes: `animal.registered`, `animal.status.changed`

### 4.2 Health Service (Port 8082)
- Vaccination records & schedule management
- Treatment records
- Vet visit logging
- Publishes: `vaccination.due`, `treatment.started`, `health.alert`
- Subscribes: `animal.registered` (to create initial health profile)

### 4.3 Breeding Service (Port 8083)
- Mating event recording
- Pregnancy tracking with auto-calculated due dates
- Birth record & offspring registration (calls Animal Service)
- Publishes: `pregnancy.confirmed`, `birth.due`, `birth.recorded`

### 4.4 Milk Production Service (Port 8084)
- Daily milk yield entry (per session)
- Lactation cycle management
- Production analytics (weekly/monthly trends, per-animal comparison)
- Uses TimescaleDB for efficient time-series queries

### 4.5 Inventory Service (Port 8085)
- Feed item catalogue
- Stock level management
- Procurement records
- Feed consumption logging
- Publishes: `inventory.low.stock` when stock < minimumStock

### 4.6 Finance Service (Port 8086)
- Financial transaction recording
- Category-wise expense tracking
- Profit/Loss computation
- Animal-level financial summary
- Immutable records with reversal pattern

### 4.7 Notification Service (Port 8087)
- Subscribes to all domain events
- Routes to Email (SendGrid), SMS (Twilio), Push (Firebase FCM)
- Notification preference management per user
- Notification history & read status

### 4.8 Report Service (Port 8088)
- Aggregates data across services via read replicas
- Pre-computed dashboards cached in Redis
- PDF generation (JasperReports)
- Export to Excel/CSV

---

## 5. Cross-Cutting Concerns

### 5.1 Authentication & Authorization
```
Client → API Gateway
         → Validates JWT with Keycloak
         → Extracts roles (OWNER, WORKER, VET, ACCOUNTANT)
         → Passes X-User-Id, X-User-Role headers downstream
         → Each service enforces method-level @PreAuthorize
```

### 5.2 Error Handling Strategy
- All services return RFC 7807 Problem Details JSON
- Global `@ControllerAdvice` per service
- Circuit breaker (Resilience4j) on all inter-service HTTP calls
- Dead letter queue (DLQ) for failed Kafka messages

### 5.3 Caching Strategy (Redis)
| Data | TTL | Invalidation |
|---|---|---|
| Animal profiles | 30 min | On update event |
| Herd listings | 15 min | On animal add/remove |
| Dashboard stats | 5 min | Scheduled refresh |
| User sessions | 24 hrs | On logout |

### 5.4 File Storage
- All uploaded documents (vet reports, receipts, photos) stored in MinIO/S3
- Pre-signed URLs for secure temporary access (15 min expiry)
- References stored as URLs in DB

---

## 6. Data Flow Examples

### Animal Registration Flow
```
Worker (Mobile) 
  → POST /animals 
  → API Gateway (auth check) 
  → Animal Service (validate, persist) 
  → Kafka: animal.registered 
  → Health Service (creates health profile) 
  → Notification Service (notifies owner of new animal added)
```

### Low Stock Alert Flow
```
Worker logs feed consumption
  → Inventory Service updates stock
  → Stock < minimumStock threshold
  → Kafka: inventory.low.stock
  → Notification Service
  → Email/SMS to Farm Owner
```
