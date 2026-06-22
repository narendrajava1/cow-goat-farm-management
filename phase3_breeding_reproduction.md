# Phase 3 - Breeding and Reproduction

## Story 1: Register breeding event
{
  "id": "STORY-014",
  "epic": "Breeding Management",
  "title": "Register breeding event",
  "description": "As a farm manager, I want to register a breeding event for animals so that reproductive history is properly tracked.",
  "acceptance_criteria": [
    "Given valid mating pair and date, When POST /api/v1/breeding/events is called, Then breeding event is created and 201 returned",
    "Given invalid animal IDs, When POST /api/v1/breeding/events is called, Then 404 error is returned",
    "Given invalid breeding data, When POST /api/v1/breeding/events is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create BreedingEvent JPA entity with mating pair and date fields",
    "Create BreedingEventRepository with findByAnimalIds",
    "Create BreedingEventService.registerBreedingEvent() method",
    "Create BreedingEventController POST /api/v1/breeding/events",
    "Write Liquibase migration V5__create_breeding_events.sql",
    "Write unit tests for BreedingEventService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "BreedingEvent",
    "service": "BreedingEventService",
    "repository": "BreedingEventRepository",
    "controller": "BreedingEventController",
    "dto_request": "BreedingEventCreateRequest",
    "dto_response": "BreedingEventResponse",
    "migration": "V5__create_breeding_events.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 3,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 3 - Breeding and Reproduction"
}

## Story 2: View breeding history
{
  "id": "STORY-015",
  "epic": "Breeding Management",
  "title": "View breeding history",
  "description": "As a farm manager, I want to view the complete breeding history of an animal so that I can track reproductive patterns.",
  "acceptance_criteria": [
    "Given valid animal ID, When GET /api/v1/animals/{animalId}/breeding is called, Then all breeding records for that animal are returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId}/breeding is called, Then 404 error is returned",
    "Given no breeding history exists, When GET /api/v1/animals/{animalId}/breeding is called, Then empty list is returned"
  ],
  "technical_tasks": [
    "Create BreedingEventController GET /api/v1/animals/{animalId}/breeding",
    "Create BreedingEventService.getBreedingHistoryByAnimalId() method",
    "Write unit tests for BreedingEventService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "BreedingEvent",
    "service": "BreedingEventService",
    "repository": "BreedingEventRepository",
    "controller": "BreedingEventController",
    "dto_request": "",
    "dto_response": "BreedingEventListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 3,
  "dependencies": ["STORY-014"],
  "phase": "Phase 3 - Breeding and Reproduction"
}

## Story 3: Register pregnancy confirmation
{
  "id": "STORY-016",
  "epic": "Breeding Management",
  "title": "Register pregnancy confirmation",
  "description": "As a veterinarian, I want to confirm a pregnancy after a breeding event so that the system accurately tracks reproductive status.",
  "acceptance_criteria": [
    "Given valid breeding event ID and confirmation date, When POST /api/v1/breeding/{eventId}/pregnancy is called, Then pregnancy is confirmed and 201 returned",
    "Given invalid event ID, When POST /api/v1/breeding/{eventId}/pregnancy is called, Then 404 error is returned",
    "Given already confirmed pregnancy, When POST /api/v1/breeding/{eventId}/pregnancy is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create Pregnancy JPA entity with confirmation date and due date fields",
    "Create PregnancyRepository with findByBreedingEventId",
    "Create PregnancyService.confirmPregnancy() method",
    "Create PregnancyController POST /api/v1/breeding/{eventId}/pregnancy",
    "Write Liquibase migration V6__create_pregnancies.sql",
    "Write unit tests for PregnancyService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Pregnancy",
    "service": "PregnancyService",
    "repository": "PregnancyRepository",
    "controller": "PregnancyController",
    "dto_request": "PregnancyConfirmRequest",
    "dto_response": "PregnancyResponse",
    "migration": "V6__create_pregnancies.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 3,
  "dependencies": ["STORY-014", "STORY-015"],
  "phase": "Phase 3 - Breeding and Reproduction"
}

## Story 4: View pregnancy status
{
  "id": "STORY-017",
  "epic": "Breeding Management",
  "title": "View pregnancy status",
  "description": "As a farm worker, I want to view the current pregnancy status of animals so that I can provide appropriate care.",
  "acceptance_criteria": [
    "Given valid animal ID, When GET /api/v1/animals/{animalId}/pregnancy is called, Then current pregnancy status is returned",
    "Given no active pregnancy, When GET /api/v1/animals/{animalId}/pregnancy is called, Then empty response or NOT_PREGNANT status is returned"
  ],
  "technical_tasks": [
    "Create PregnancyController GET /api/v1/animals/{animalId}/pregnancy",
    "Create PregnancyService.getPregnancyStatusByAnimalId() method",
    "Write unit tests for PregnancyService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Pregnancy",
    "service": "PregnancyService",
    "repository": "PregnancyRepository",
    "controller": "PregnancyController",
    "dto_request": "",
    "dto_response": "PregnancyStatusResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 3,
  "sprint": 3,
  "dependencies": ["STORY-016"],
  "phase": "Phase 3 - Breeding and Reproduction"
}

## Story 5: Register birth event
{
  "id": "STORY-018",
  "epic": "Breeding Management",
  "title": "Register birth event",
  "description": "As a farm worker, I want to register the birth of offspring from a pregnancy so that reproductive history is complete.",
  "acceptance_criteria": [
    "Given valid pregnancy ID and birth details, When POST /api/v1/births is called, Then birth event is created and 201 returned",
    "Given invalid pregnancy ID, When POST /api/v1/births is called, Then 404 error is returned",
    "Given child animal creation, When POST /api/v1/births is called, Then new animals are created in registry"
  ],
  "technical_tasks": [
    "Create BirthEvent JPA entity with offspring details and birth date fields",
    "Create BirthEventRepository with findByPregnancyId",
    "Create BirthEventService.registerBirth() method",
    "Create BirthEventController POST /api/v1/births",
    "Write Liquibase migration V7__create_birth_events.sql",
    "Write unit tests for BirthEventService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "BirthEvent",
    "service": "BirthEventService",
    "repository": "BirthEventRepository",
    "controller": "BirthEventController",
    "dto_request": "BirthEventCreateRequest",
    "dto_response": "BirthEventResponse",
    "migration": "V7__create_birth_events.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 3,
  "dependencies": ["STORY-016", "STORY-017"],
  "phase": "Phase 3 - Breeding and Reproduction"
}

## Story 6: Track breeding cycle
{
  "id": "STORY-019",
  "epic": "Breeding Management",
  "title": "Track breeding cycle",
  "description": "As a farm manager, I want to track the breeding cycle of animals so that breeding programs can be planned effectively.",
  "acceptance_criteria": [
    "Given animal ID and date range, When GET /api/v1/animals/{animalId}/breeding/cycle is called, Then breeding cycle information is returned",
    "Given valid breeding records, When GET /api/v1/breeding/cycles is called, Then summary of breeding cycles is returned"
  ],
  "technical_tasks": [
    "Create BreedingEventController GET /api/v1/animals/{animalId}/breeding/cycle and GET /api/v1/breeding/cycles",
    "Create BreedingEventService.getBreedingCycleByAnimalId() method",
    "Write unit tests for BreedingEventService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "BreedingEvent",
    "service": "BreedingEventService",
    "repository": "BreedingEventRepository",
    "controller": "BreedingEventController",
    "dto_request": "",
    "dto_response": "BreedingCycleResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 3,
  "dependencies": ["STORY-014", "STORY-015"],
  "phase": "Phase 3 - Breeding and Reproduction"
}