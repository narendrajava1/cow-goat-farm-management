# 07 — Implementation Plan

## 1. Delivery Philosophy

- **Iterative delivery** — working software at end of each phase
- **Vertical slices** — each phase delivers end-to-end functionality, not just backend
- **API-first** — contracts agreed before implementation
- **Test-driven** — unit + integration tests mandatory per service
- **Infrastructure as Code** — all environments reproducible from day one

---

## 2. Team Composition (Recommended)

| Role | Count | Responsibilities |
|---|---|---|
| Tech Lead / Architect | 1 | Architecture decisions, code reviews, cross-cutting concerns |
| Backend Engineers (Spring Boot) | 3 | Microservices implementation |
| Frontend Engineer (React) | 1 | Web dashboard |
| Mobile Engineer (React Native) | 1 | Mobile app |
| DevOps Engineer | 1 | CI/CD, Kubernetes, monitoring |
| QA Engineer | 1 | Test plans, automation |
| **Total** | **8** | |

---

## 3. Phased Implementation Plan

### Phase 0 — Foundation (Week 1-2)
**Goal:** Infrastructure ready, skeleton services running, CI/CD green

| Task | Owner | Effort |
|---|---|---|
| Set up GitHub monorepo structure | DevOps | 1d |
| Docker Compose for local dev (Postgres, Redis, Kafka, Keycloak) | DevOps | 2d |
| GitHub Actions CI pipeline (build, test, lint) | DevOps | 1d |
| Keycloak realm setup (roles, clients) | Backend Lead | 1d |
| Spring Boot project scaffolding (all 8 services) | Backend Lead | 2d |
| Liquibase migration setup per service | Backend | 1d |
| API Gateway routing + JWT filter | Backend Lead | 2d |
| farm-common library (shared DTOs, events) | Backend Lead | 1d |
| React app scaffold + auth integration | Frontend | 2d |
| React Native scaffold | Mobile | 2d |

**Milestone:** Local dev environment starts with `docker-compose up`. All services boot and health endpoints return 200.

---

### Phase 1 — Animal Registry (Week 3-5)
**Goal:** Complete animal management — register, view, update, track herds

#### Backend
- Animal entity, repository, service, controller
- Herd management
- Farm & User management
- Tag number validation
- Animal status transitions
- Redis caching for animal profiles
- Kafka publishing: `animal.registered`, `animal.status.changed`
- Liquibase migrations: V1-V3

#### Frontend / Mobile
- Animal list screen with search/filter
- Animal registration form
- Animal profile detail view
- Herd management screen

#### Testing
- Unit tests: AnimalService (tag uniqueness, status transitions)
- Integration tests: full CRUD via TestContainers (Postgres + Redis)
- API contract tests (Spring Cloud Contract)

**Milestone:** Farm worker can register a cow/goat from mobile app and view its profile on web dashboard.

---

### Phase 2 — Health Management (Week 6-8)
**Goal:** Full health record tracking with automated vaccination alerts

#### Backend
- Health Service: health records, vaccinations, treatments, vet visits
- Vaccination schedule management
- `upcoming-due` query endpoint
- Scheduled job: scan due vaccinations daily at 8am → publish to Kafka
- Notification Service: basic Email via SendGrid (vaccination due alerts)
- Kafka consumer in Health Service for `animal.registered` events

#### Frontend / Mobile
- Health history timeline per animal
- Add vaccination / treatment / vet visit forms
- Upcoming alerts dashboard widget
- Email notification delivery

**Milestone:** When a vaccination is logged with a next-due date, the system automatically sends an email alert 7 days before it's due.

---

### Phase 3 — Breeding & Reproduction (Week 9-11)
**Goal:** Track breeding cycle from mating to birth, auto-register offspring

#### Backend
- Breeding Service: mating events, pregnancy records, birth records
- Auto gestation calculation (COW=280d, GOAT=150d)
- Birth recording → calls Animal Service to register offspring
- Pregnancy due alerts (30 days, 7 days, 1 day before)
- Circuit breaker on Animal Service calls

#### Frontend / Mobile
- Mating event form
- Active pregnancies dashboard
- Birth recording form
- Offspring auto-populated after birth recording

**Milestone:** Recording a mating event creates a pregnancy with auto-calculated due date. Recording birth auto-registers offspring animals and links their parentage.

---

### Phase 4 — Milk Production (Week 12-14)
**Goal:** Daily milk yield tracking with lactation analytics

#### Backend
- Milk Service with TimescaleDB
- Per-session milk logging
- Bulk entry endpoint
- Lactation cycle management (auto-create on birth, close on drying-off)
- Continuous aggregates for daily/weekly totals
- Production analytics API (trends, per-animal comparisons)

#### Frontend / Mobile
- Daily milk entry grid (all animals × sessions)
- Milk production charts (line chart by animal, farm total)
- Lactation cycle view per animal
- Top/bottom producers report

**Milestone:** Worker logs morning/evening milk for all 30 cows in under 2 minutes. Dashboard shows farm total for the day and weekly trend chart.

---

### Phase 5 — Feed & Inventory (Week 15-16)
**Goal:** Feed stock management with low-stock alerts

#### Backend
- Inventory Service: feed catalogue, stock levels, consumption, procurement
- Stock calculation on consumption log
- Low-stock alert trigger → Kafka → Notification Service
- SMS notifications via Twilio (in addition to email)

#### Frontend / Mobile
- Feed stock dashboard (traffic light status)
- Consumption logging form
- Procurement recording
- Low stock alert card on dashboard

**Milestone:** When maize silage drops below minimum stock, owner receives SMS + email alert within 60 seconds.

---

### Phase 6 — Financial Management (Week 17-19)
**Goal:** Income/expense tracking with P&L reports

#### Backend
- Finance Service: transactions, categories, reversal pattern
- Per-animal P&L endpoint
- Monthly/yearly financial summary
- Financial transactions triggered by: milk sales, animal sales (from events)

#### Frontend / Mobile
- Transaction entry form
- Income vs Expense bar chart
- Category breakdown pie chart
- Per-animal financial summary table
- Date-range P&L report

**Milestone:** Owner can see total income, total expense, and net profit for any date range broken down by category.

---

### Phase 7 — Reports, Analytics & Polish (Week 20-22)
**Goal:** PDF reports, consolidated dashboards, offline mobile support

#### Backend
- Report Service: JasperReports PDF generation
  - Animal health certificate
  - Vaccination history report
  - Milk production monthly report
  - Financial statement
- Dashboard aggregation API (cached in Redis)
- Push notifications via Firebase FCM

#### Frontend
- Executive dashboard (6 KPI cards + 3 charts)
- Report download centre
- Notification centre with read/unread

#### Mobile
- WatermelonDB offline sync (animal list, milk entry)
- Push notification integration
- Offline indicator + sync status

**Milestone:** All core reports downloadable as PDF. Mobile app works fully offline and syncs when internet is restored.

---

### Phase 8 — Hardening & Go-Live (Week 23-24)
**Goal:** Production-ready deployment

| Task | Owner |
|---|---|
| Kubernetes manifests (all services) | DevOps |
| Horizontal Pod Autoscaler config | DevOps |
| Prometheus + Grafana dashboards | DevOps |
| Loki log aggregation | DevOps |
| Jaeger distributed tracing | DevOps |
| Security audit (OWASP Top 10 review) | Backend Lead |
| Load testing (k6) — 100 concurrent users | QA |
| Data migration scripts (from existing paper/Excel) | Backend |
| User acceptance testing with farm team | QA + Stakeholders |
| Production deployment + monitoring bake | DevOps + All |

**Milestone:** System live in production, processing real farm data, monitored with alerts configured.

---

## 4. Effort Summary

| Phase | Duration | Backend | Frontend | Mobile | DevOps | QA |
|---|---|---|---|---|---|---|
| 0 – Foundation | 2w | 6d | 2d | 2d | 5d | - |
| 1 – Animal Registry | 3w | 10d | 6d | 6d | 1d | 3d |
| 2 – Health | 3w | 10d | 5d | 5d | 1d | 3d |
| 3 – Breeding | 3w | 10d | 5d | 5d | 1d | 3d |
| 4 – Milk | 3w | 10d | 6d | 6d | 1d | 3d |
| 5 – Inventory | 2w | 6d | 4d | 4d | 1d | 2d |
| 6 – Finance | 3w | 10d | 6d | 4d | 1d | 3d |
| 7 – Reports | 3w | 8d | 6d | 6d | 2d | 4d |
| 8 – Hardening | 2w | 4d | 2d | 2d | 8d | 6d |
| **Total** | **24w** | **74d** | **42d** | **40d** | **21d** | **27d** |

---

## 5. Definition of Done (per story)

- [ ] Code reviewed and approved by at least 1 peer
- [ ] Unit test coverage ≥ 80% for new code
- [ ] Integration test covering happy path + key error cases
- [ ] API documented in OpenAPI spec
- [ ] Liquibase migration included (if DB change)
- [ ] No critical SonarQube issues
- [ ] Feature tested on local docker-compose environment
- [ ] Deployed to dev environment via CI/CD

---

## 6. Key Dependencies & Risks (Summary)

| Dependency | Risk | Mitigation |
|---|---|---|
| TimescaleDB setup | First time for team | Spike in Phase 0 |
| Keycloak configuration | Complex RBAC | DevOps lead owns Week 1 |
| Mobile offline sync | High complexity | WatermelonDB PoC in Phase 0 |
| Cross-service data consistency | Eventual consistency challenges | Event sourcing + idempotency keys |
| Farm internet connectivity | Mobile must work offline | Phase 7 offline-first feature |
