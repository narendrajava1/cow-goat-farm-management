# 01 — System Overview

## 1. Executive Summary

The **Cow-Goat Farm Management System (CGFMS)** is a full-stack, cloud-ready application designed to digitize and automate end-to-end farm operations for small-to-mid-sized livestock farms managing cows and goats.

The system replaces paper-based record keeping with a centralized, real-time platform covering animal health, breeding, milk production, feed inventory, and financial management — accessible from both web and mobile devices.

---

## 2. Problem Statement

| Current Pain Point | Impact |
|---|---|
| Manual register books for animal records | Data loss, no historical trends |
| No vaccination schedule tracking | Disease outbreaks, missed doses |
| Ad-hoc milk production logging | Cannot identify low-yield animals |
| No feed inventory visibility | Over-purchasing or stockouts |
| Scattered expense records | No accurate profit/loss per animal |
| No breeding cycle tracking | Missed mating windows, low reproduction rates |

---

## 3. Goals & Objectives

### Primary Goals
- Centralize all animal data in one platform accessible 24/7
- Automate health, breeding, and production alerts
- Provide financial clarity per animal and per herd

### Secondary Goals
- Enable data-driven decisions via analytics dashboards
- Support offline-first mobile usage for field workers
- Scale from a single farm to multi-farm operations

---

## 4. Scope

### In Scope (v1.0)
- Animal registry (cows & goats) with full profile management
- Health records — vaccinations, treatments, vet visits
- Breeding & reproduction tracking
- Milk production logging & lactation analysis
- Feed & inventory management
- Financial records — purchases, sales, expenses
- Alerts & notifications (email, SMS, in-app push)
- Role-based access control (Owner, Worker, Vet, Accountant)
- Web dashboard + mobile app (iOS & Android)

### Out of Scope (v1.0 — future roadmap)
- IoT sensor integration (milk meters, weight sensors)
- AI/ML-based disease prediction
- Multi-language support (post v1.0)
- Marketplace/trading portal for buying/selling animals

---

## 5. Stakeholders

| Role | Responsibility | Key Concerns |
|---|---|---|
| Farm Owner | Business decisions, financial oversight | ROI, profit visibility |
| Farm Worker | Daily data entry (feeding, milk, observations) | Simple UI, mobile-first |
| Veterinarian | Health records, treatment management | Medical history accuracy |
| Accountant | Financial reporting | Audit trail, export to Excel |
| System Admin | User management, configuration | Security, uptime |

---

## 6. Assumptions & Constraints

### Assumptions
- Farm has basic internet connectivity (mobile data acceptable)
- Each animal has a unique physical tag (ear tag / RFID)
- At least one person per farm is designated as system admin
- Single currency per farm installation

### Constraints
- Must work on low-bandwidth mobile networks (offline sync for mobile)
- Initial deployment targets farms with 10–500 animals
- Data must be retained for minimum 7 years (regulatory compliance)
- All animal health data must be exportable in PDF for veterinary authorities
