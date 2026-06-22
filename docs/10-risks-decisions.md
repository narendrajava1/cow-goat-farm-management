# 10 — Risks, Decisions & Open Questions

## 1. Risk Register

| ID | Risk | Probability | Impact | Mitigation | Owner |
|---|---|---|---|---|---|
| R-001 | Farm has poor internet connectivity — mobile app unusable | High | High | Offline-first mobile (WatermelonDB sync) — Phase 7 | Mobile Lead |
| R-002 | TimescaleDB learning curve causes milk service delays | Medium | Medium | Spike in Phase 0 week 1 before committing | Backend Lead |
| R-003 | Kafka complexity slows team for first 2 phases | Medium | Medium | Use simple REST during Phase 1, migrate to Kafka in Phase 2 | Arch Lead |
| R-004 | Farm workers resist adopting digital system | High | High | Mobile-first, offline, simple UX, training sessions included in rollout | Product |
| R-005 | Keycloak RBAC misconfiguration causes security gaps | Low | High | Security review after Phase 0, penetration test before go-live | DevOps |
| R-006 | Free-tier cloud costs spike with multiple Postgres instances | Medium | Medium | Consolidate to single Postgres with separate schemas for Phase 1, split later | DevOps |
| R-007 | Data migration from Excel/paper introduces incorrect data | High | Medium | Migration scripts with validation + dry-run + farm owner sign-off | Backend |
| R-008 | SMS (Twilio) costs become significant at scale | Low | Low | Rate-limit SMS to critical alerts only; email for routine | Backend |

---

## 2. Architecture Decision Records (ADRs)

### ADR-001: Microservices vs Modular Monolith

**Status:** Decided  
**Context:** Team of 8, 24-week timeline. Microservices add operational complexity but enable independent scaling and deployment.  
**Decision:** **Start with a modular monolith** (single Spring Boot app, separate packages per domain) for Phase 1-3. Extract to true microservices in Phase 4+ as team familiarity grows and service boundaries are proven.  
**Rationale:** Martin Fowler's "MonolithFirst" pattern — don't split what you don't yet fully understand. Reduces Docker/K8s overhead in early phases.  
**Consequence:** Database per service still respected from day 1 (separate schemas, separate DataSources). API contracts documented as if services are separate. Easy to extract later.

---

### ADR-002: PostgreSQL for All Relational Data

**Status:** Decided  
**Context:** Considered MySQL vs PostgreSQL for operational data.  
**Decision:** **PostgreSQL 16** for all relational data.  
**Rationale:** Better JSON support, Row-Level Security for multi-tenancy, TimescaleDB extension for milk time-series, `gen_random_uuid()` built-in, superior indexing options.  
**Consequence:** Team needs PostgreSQL knowledge (not MySQL). Migration scripts (Flyway) must be Postgres-specific SQL.

---

### ADR-003: TimescaleDB for Milk Production Data

**Status:** Decided  
**Context:** Milk records are time-series: high write volume (3 sessions/day × N animals × 365 days), range queries dominate reads.  
**Decision:** **TimescaleDB** (PostgreSQL extension) with hypertables for milk_production.  
**Rationale:** Automatic partitioning by time, continuous aggregates for daily/weekly summaries, no extra infrastructure — it's just a Postgres extension.  
**Consequence:** TimescaleDB-specific DDL in migrations. Docker image must use `timescale/timescaledb` instead of `postgres`.  
**Alternative considered:** InfluxDB — rejected because it requires a separate query language (Flux) and adds operational complexity.

---

### ADR-004: Kafka for Async Events

**Status:** Decided  
**Context:** Services need to react to events from other services (e.g. Health Service creates health profile when new animal is registered).  
**Decision:** **Apache Kafka** for all async inter-service communication.  
**Rationale:** Durable, replayable events. If Notification Service is down, it catches up on restart. Decouples producer from consumer availability.  
**Consequence:** Kafka is required infrastructure from Phase 2 onwards. Messages must be idempotent (consumers may re-process on restart).  
**Alternative considered:** RabbitMQ — rejected because it lacks durable replay and log compaction.

---

### ADR-005: Keycloak for Auth

**Status:** Decided  
**Context:** Need role-based access control with OAuth2/JWT across multiple services.  
**Decision:** **Keycloak** as the identity provider, self-hosted.  
**Rationale:** Mature, open-source, Spring Boot integration via `spring-boot-starter-oauth2-resource-server` is straightforward. Supports multiple realms (one per farm in future multi-tenant mode).  
**Consequence:** Keycloak adds a service to maintain. Realm export must be version-controlled.  
**Alternative considered:** AWS Cognito — rejected because it creates cloud vendor lock-in and is more complex to configure for fine-grained roles.

---

### ADR-006: Flyway for DB Migrations

**Status:** Decided  
**Context:** Need deterministic, versioned DB schema management.  
**Decision:** **Liquibase** — versioned SQL migrations.  
**Rationale:** SQL-first (no DSL to learn), integrates with Spring Boot auto-run on startup, version-controlled alongside code.  
**Consequence:** `spring.jpa.hibernate.ddl-auto=validate` in all environments — Hibernate never creates/alters tables, Liquibase owns schema.

---

### ADR-007: MapStruct for DTO Mapping

**Status:** Decided  
**Context:** Need to map between JPA entities and DTOs in every service.  
**Decision:** **MapStruct** annotation-based mapping.  
**Rationale:** Compile-time generated code (no reflection), type-safe, easy to add custom mappings, zero runtime overhead.  
**Consequence:** `@Mapper` interface per service. Build fails on mapping errors (good).  
**Alternative considered:** ModelMapper — rejected due to reflection-based runtime mapping and harder debugging.

---

## 3. Open Questions (to resolve before implementation)

| # | Question | Impact if Unresolved | Deadline |
|---|---|---|---|
| OQ-001 | Single farm per installation OR multi-tenant SaaS? | Affects DB schema design (`farm_id` on all tables vs separate DBs) | Before Phase 0 ends |
| OQ-002 | Which cloud provider for production hosting? | Affects managed Postgres choice, storage, K8s flavor | Before Phase 7 |
| OQ-003 | What is the target mobile OS? iOS only, Android only, or both? | Mobile effort doubles for both | Phase 0 kickoff |
| OQ-004 | Does the system need to integrate with government animal registration portals? | Separate integration service needed | Before Phase 1 |
| OQ-005 | What is the preferred language for mobile UI? (English / Regional) | i18n setup needed from Phase 1 if regional | Phase 0 kickoff |
| OQ-006 | Are there existing Excel sheets to migrate? | Data migration effort could be significant (2-4 weeks) | Before Phase 8 |
| OQ-007 | Do vets need to access the system from outside the farm network? | Keycloak public endpoint + SSL cert needed | Before Phase 2 |

---

## 4. Future Roadmap (Post v1.0)

| Feature | Value | Complexity | Target |
|---|---|---|---|
| IoT milk meter integration | Auto-log milk yield, no manual entry | High | v1.5 |
| AI disease prediction | Early warning based on health patterns | Very High | v2.0 |
| Multi-farm dashboard | Owners with multiple farms see consolidated view | Medium | v1.5 |
| Marketplace (buy/sell animals) | Peer-to-peer animal trading | High | v2.0 |
| Regional language support | Hindi, Tamil, Marathi, Kannada | Medium | v1.3 |
| RFID/barcode scanning (mobile) | Scan ear tag to pull up animal profile | Medium | v1.2 |
| WhatsApp notifications | High adoption in rural India | Low | v1.1 |
| Veterinary authority exports | Auto-generate compliance PDFs | Medium | v1.2 |
