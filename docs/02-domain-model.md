# 02 — Domain Model

## 1. Core Entities Overview

```
Animal ◄──────────────────────────────────────────┐
  │                                               │
  ├──► HealthRecord                               │
  │       ├──► Vaccination                        │
  │       ├──► Treatment                          │
  │       └──► VetVisit                           │
  │                                               │
  ├──► BreedingRecord                             │
  │       ├──► MatingEvent                        │
  │       ├──► PregnancyRecord                    │
  │       └──► BirthRecord ──────────────────────►┘ (offspring = new Animal)
  │
  ├──► MilkProductionRecord
  │       └──► LactationCycle
  │
  ├──► FeedConsumption
  │
  └──► FinancialRecord
          ├──► PurchaseRecord (when animal was bought)
          └──► SaleRecord (when animal is sold)

FeedInventory
  ├──► FeedStock
  └──► FeedProcurement

Farm
  ├──► Herd (group of animals)
  └──► User (farm staff)
```

---

## 2. Entity Definitions

### 2.1 Animal

The central entity. Every cow or goat is an `Animal`.

```
Animal {
  id              UUID          PK
  tagNumber       String        UNIQUE — physical ear tag / RFID
  name            String        optional (pet name)
  type            Enum          COW | GOAT
  breed           String        e.g. Holstein, Nubian, Boer
  gender          Enum          MALE | FEMALE
  dateOfBirth     Date
  colorMarkings   String
  weight          Decimal       kg, updated periodically
  status          Enum          ACTIVE | SOLD | DECEASED | QUARANTINED
  acquisitionType Enum          BORN_ON_FARM | PURCHASED
  motherId        UUID          FK → Animal (nullable)
  fatherId        UUID          FK → Animal (nullable)
  herdId          UUID          FK → Herd
  farmId          UUID          FK → Farm
  createdAt       Timestamp
  updatedAt       Timestamp
}
```

---

### 2.2 HealthRecord

```
HealthRecord {
  id              UUID
  animalId        UUID          FK → Animal
  recordType      Enum          VACCINATION | TREATMENT | VET_VISIT | OBSERVATION
  recordDate      Date
  description     String
  diagnosedBy     String        vet name / worker name
  notes           Text
  nextDueDate     Date          nullable — for recurring items
  attachments     String[]      file URLs (PDFs, images)
}

Vaccination {
  id              UUID
  healthRecordId  UUID          FK → HealthRecord
  vaccineName     String
  batchNumber     String
  dosage          String
  administeredBy  String
  nextDoseDate    Date
}

Treatment {
  id              UUID
  healthRecordId  UUID
  medicineName    String
  dosage          String
  frequency       String        e.g. "twice daily for 5 days"
  startDate       Date
  endDate         Date
  cost            Decimal
}
```

---

### 2.3 BreedingRecord

```
MatingEvent {
  id              UUID
  femaleAnimalId  UUID
  maleAnimalId    UUID          nullable (if external bull/buck)
  externalSireId  String        nullable (AI straw ID if artificial insemination)
  matingDate      Date
  matingType      Enum          NATURAL | ARTIFICIAL_INSEMINATION
  notes           Text
}

PregnancyRecord {
  id              UUID
  matingEventId   UUID
  femaleAnimalId  UUID
  expectedDueDate Date          matingDate + gestation period (cow=280d, goat=150d)
  confirmedDate   Date          vet confirmation date
  status          Enum          SUSPECTED | CONFIRMED | ABORTED | DELIVERED
}

BirthRecord {
  id              UUID
  pregnancyId     UUID
  birthDate       Date
  numberOfOffspring Int
  offspringIds    UUID[]        FK → Animal[] (newly registered)
  complications   Text
  attendedBy      String
}
```

---

### 2.4 MilkProduction

```
MilkProductionRecord {
  id              UUID
  animalId        UUID
  recordDate      Date
  session         Enum          MORNING | EVENING | AFTERNOON
  quantityLiters  Decimal
  fatPercentage   Decimal       optional
  recordedBy      UUID          FK → User
}

LactationCycle {
  id              UUID
  animalId        UUID
  startDate       Date          day after birth
  endDate         Date          drying-off date
  totalYieldLiters Decimal      computed
  peakYield       Decimal
  lactationNumber Int           1st, 2nd, 3rd lactation
}
```

---

### 2.5 FeedInventory

```
FeedItem {
  id              UUID
  name            String        e.g. "Maize Silage", "Concentrate Mix"
  category        Enum          ROUGHAGE | CONCENTRATE | SUPPLEMENT | MINERAL
  unit            String        kg | litre | bag
  currentStock    Decimal
  minimumStock    Decimal       alert threshold
  costPerUnit     Decimal
}

FeedConsumption {
  id              UUID
  animalId        UUID          nullable (herd-level or per-animal)
  herdId          UUID          nullable
  feedItemId      UUID
  consumptionDate Date
  quantityUsed    Decimal
  recordedBy      UUID
}

FeedProcurement {
  id              UUID
  feedItemId      UUID
  purchaseDate    Date
  quantityPurchased Decimal
  totalCost       Decimal
  supplierName    String
  invoiceNumber   String
}
```

---

### 2.6 Financial

```
FinancialTransaction {
  id              UUID
  transactionType Enum          PURCHASE | SALE | EXPENSE | INCOME
  category        String        e.g. ANIMAL_PURCHASE, MILK_SALE, VET_FEE, FEED_PURCHASE
  animalId        UUID          nullable (if animal-specific)
  amount          Decimal
  transactionDate Date
  description     Text
  receiptUrl      String
  recordedBy      UUID
}
```

---

### 2.7 Farm & Users

```
Farm {
  id              UUID
  name            String
  ownerName       String
  location        String
  contactPhone    String
  contactEmail    String
  currency        String        default: INR
  timezone        String
}

Herd {
  id              UUID
  farmId          UUID
  name            String        e.g. "Milking Herd", "Dry Herd", "Kids Pen"
  type            Enum          COW | GOAT | MIXED
}

User {
  id              UUID
  farmId          UUID
  name            String
  email           String
  phone           String
  role            Enum          OWNER | WORKER | VET | ACCOUNTANT | ADMIN
  isActive        Boolean
}
```

---

## 3. Key Business Rules

| Rule | Description |
|---|---|
| BR-001 | A female animal can only have one active pregnancy at a time |
| BR-002 | Milk production records are only valid for FEMALE animals in active lactation |
| BR-003 | Tag number must be globally unique per farm |
| BR-004 | Vaccination next-due alerts trigger 7 days before due date |
| BR-005 | When stock falls below `minimumStock`, a low-stock alert is triggered |
| BR-006 | An animal with status SOLD or DECEASED cannot have new health/milk records |
| BR-007 | Gestation period: Cow = 280 days, Goat = 150 days (auto-calculated on mating entry) |
| BR-008 | Financial transactions are immutable once recorded (corrections via reversal entries) |
