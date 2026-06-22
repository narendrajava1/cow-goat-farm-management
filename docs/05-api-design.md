# 05 — API Design

## 1. API Standards

- **Style:** RESTful JSON API
- **Versioning:** URI-based (`/api/v1/...`)
- **Auth:** Bearer JWT in `Authorization` header
- **Error Format:** RFC 7807 Problem Details
- **Pagination:** Cursor-based for large datasets, offset for small
- **Date Format:** ISO 8601 (`2026-06-22`, `2026-06-22T10:30:00Z`)

### Standard Error Response
```json
{
  "type": "https://cgfms.io/errors/validation-error",
  "title": "Validation Failed",
  "status": 422,
  "detail": "tag_number must be unique within the farm",
  "instance": "/api/v1/animals",
  "timestamp": "2026-06-22T10:30:00Z",
  "errors": [
    { "field": "tagNumber", "message": "Tag TS-001 already exists" }
  ]
}
```

---

## 2. Animal Service APIs

### Register Animal
```
POST /api/v1/animals
Authorization: Bearer <jwt>
Roles: OWNER, WORKER

Request:
{
  "tagNumber": "COW-001",
  "name": "Lakshmi",
  "animalType": "COW",
  "breed": "Holstein Friesian",
  "gender": "FEMALE",
  "dateOfBirth": "2022-03-15",
  "colorMarkings": "Black and white patches",
  "weightKg": 320.5,
  "acquisitionType": "PURCHASED",
  "herdId": "uuid-herd-1",
  "notes": "Purchased from ABC Farm"
}

Response: 201 Created
{
  "id": "uuid-animal-1",
  "tagNumber": "COW-001",
  "name": "Lakshmi",
  "animalType": "COW",
  "status": "ACTIVE",
  "age": { "years": 4, "months": 3 },
  "createdAt": "2026-06-22T10:30:00Z"
}
```

### Get Animal Profile
```
GET /api/v1/animals/{animalId}
Response: 200 OK — full animal profile with computed fields (age, lactation status, current pregnancy)
```

### List Animals
```
GET /api/v1/animals?type=COW&gender=FEMALE&status=ACTIVE&herdId=uuid&page=0&size=20
Response: 200 OK
{
  "content": [ ...animals ],
  "pagination": { "page": 0, "size": 20, "total": 145 }
}
```

### Update Animal
```
PUT /api/v1/animals/{animalId}
Roles: OWNER, WORKER
— partial updates supported
```

### Change Animal Status
```
PATCH /api/v1/animals/{animalId}/status
Roles: OWNER
{
  "status": "SOLD",
  "effectiveDate": "2026-06-20",
  "reason": "Sold to Ramesh Farm for ₹45,000"
}
```

---

## 3. Health Service APIs

### Add Health Record
```
POST /api/v1/animals/{animalId}/health-records
Roles: OWNER, WORKER, VET

{
  "recordType": "VACCINATION",
  "recordDate": "2026-06-22",
  "description": "FMD Vaccination",
  "diagnosedBy": "Dr. Sharma",
  "nextDueDate": "2026-12-22",
  "vaccination": {
    "vaccineName": "Raksha FMD",
    "batchNumber": "B2026-001",
    "dosageMl": 2.0,
    "administeredBy": "Dr. Sharma",
    "nextDoseDate": "2026-12-22"
  }
}
Response: 201 Created
```

### Get Health History
```
GET /api/v1/animals/{animalId}/health-records?type=VACCINATION&from=2025-01-01&to=2026-06-22
```

### Get Upcoming Due Items (Dashboard)
```
GET /api/v1/health/upcoming-due?farmId=uuid&days=30
Response:
{
  "vaccinationsDue": [
    { "animalId": "...", "tagNumber": "COW-001", "vaccineName": "FMD", "dueDate": "2026-07-01" }
  ],
  "treatmentsEnding": [...],
  "count": 12
}
```

---

## 4. Breeding Service APIs

### Record Mating Event
```
POST /api/v1/breeding/mating
Roles: OWNER, WORKER

{
  "femaleAnimalId": "uuid-cow-1",
  "maleAnimalId": "uuid-bull-1",
  "matingDate": "2026-06-20",
  "matingType": "NATURAL",
  "notes": "First mating after 3rd lactation"
}
Response: 201 — includes auto-calculated expected due date (2027-03-27 for cow)
```

### Confirm Pregnancy
```
PATCH /api/v1/breeding/pregnancies/{pregnancyId}/confirm
{
  "confirmedDate": "2026-07-15",
  "confirmedBy": "Dr. Patel"
}
```

### Record Birth
```
POST /api/v1/breeding/pregnancies/{pregnancyId}/birth
{
  "birthDate": "2027-03-28",
  "numberOfOffspring": 1,
  "attendedBy": "Ram Kumar",
  "complications": "None",
  "offspring": [
    { "gender": "FEMALE", "birthWeightKg": 28.5 }
  ]
}
Response: 201 — triggers auto-registration of offspring in Animal Service
```

### Active Pregnancies
```
GET /api/v1/breeding/pregnancies/active?farmId=uuid
— returns all animals currently pregnant with days remaining
```

---

## 5. Milk Production APIs

### Log Milk Yield
```
POST /api/v1/milk/records
Roles: OWNER, WORKER

{
  "animalId": "uuid-cow-1",
  "recordDate": "2026-06-22",
  "session": "MORNING",
  "quantityLiters": 8.5,
  "fatPercentage": 4.2
}
```

### Bulk Log (for multiple animals)
```
POST /api/v1/milk/records/bulk
{
  "records": [
    { "animalId": "...", "session": "MORNING", "quantityLiters": 8.5 },
    { "animalId": "...", "session": "MORNING", "quantityLiters": 6.2 }
  ],
  "recordDate": "2026-06-22"
}
```

### Production Analytics
```
GET /api/v1/milk/analytics?animalId=uuid&from=2026-01-01&to=2026-06-30&groupBy=WEEK
Response:
{
  "animalId": "uuid",
  "tagNumber": "COW-001",
  "period": { "from": "2026-01-01", "to": "2026-06-30" },
  "totalLiters": 1842.5,
  "averagePerDay": 10.1,
  "peakDay": { "date": "2026-02-14", "liters": 14.2 },
  "weeklyBreakdown": [ ... ]
}
```

### Farm-Level Daily Summary
```
GET /api/v1/milk/summary/daily?farmId=uuid&date=2026-06-22
{
  "date": "2026-06-22",
  "totalLiters": 245.8,
  "activeMilkingAnimals": 28,
  "sessionBreakdown": {
    "morning": 98.4,
    "afternoon": 72.1,
    "evening": 75.3
  }
}
```

---

## 6. Inventory APIs

### Log Feed Consumption
```
POST /api/v1/inventory/consumption
{
  "feedItemId": "uuid-feed-1",
  "herdId": "uuid-herd-1",
  "consumptionDate": "2026-06-22",
  "quantityUsed": 120.0
}
```

### Stock Status
```
GET /api/v1/inventory/stock?farmId=uuid
Response: list of all feed items with current stock vs minimum, traffic-light status (OK/LOW/CRITICAL)
```

### Record Procurement
```
POST /api/v1/inventory/procurement
{
  "feedItemId": "uuid",
  "purchaseDate": "2026-06-22",
  "quantityPurchased": 500.0,
  "totalCost": 12500.00,
  "supplierName": "Agro Traders",
  "invoiceNumber": "INV-2026-445"
}
```

---

## 7. Finance APIs

### Record Transaction
```
POST /api/v1/finance/transactions
{
  "transactionType": "INCOME",
  "category": "MILK_SALE",
  "amount": 4875.00,
  "transactionDate": "2026-06-22",
  "description": "Milk sold to dairy cooperative - 325L @ ₹15/L"
}
```

### Financial Summary
```
GET /api/v1/finance/summary?farmId=uuid&from=2026-01-01&to=2026-06-30
{
  "period": "2026-H1",
  "totalIncome": 285000.00,
  "totalExpense": 142000.00,
  "netProfit": 143000.00,
  "byCategory": {
    "MILK_SALE": 220000.00,
    "ANIMAL_SALE": 65000.00,
    "FEED_PURCHASE": -85000.00,
    "VET_FEE": -18000.00
  }
}
```

### Per-Animal P&L
```
GET /api/v1/finance/animals/{animalId}/pnl
— purchase cost vs milk income + sale price — all costs attributed
```

---

## 8. Notification APIs

```
GET /api/v1/notifications?userId=uuid&read=false&page=0&size=20
PATCH /api/v1/notifications/{id}/read
PATCH /api/v1/notifications/read-all

GET /api/v1/notifications/preferences
PUT /api/v1/notifications/preferences
{
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "subscriptions": ["VACCINATION_DUE", "LOW_STOCK", "BIRTH_DUE", "PREGNANCY_CONFIRMED"]
}
```
