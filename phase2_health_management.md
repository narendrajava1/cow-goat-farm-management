# Phase 2 - Health Management

## Story 1: Register animal health record
{
  "id": "STORY-008",
  "epic": "Animal Health",
  "title": "Register animal health record",
  "description": "As a veterinarian or farm worker, I want to register a new health record for an animal so that all health information is properly documented.",
  "acceptance_criteria": [
    "Given a valid animal ID and health record data, When POST /api/v1/animals/{animalId}/health is called, Then health record is created and 201 returned",
    "Given invalid animal ID, When POST /api/v1/animals/{animalId}/health is called, Then 404 error is returned",
    "Given missing required fields, When POST /api/v1/animals/{animalId}/health is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create HealthRecord JPA entity with all health fields",
    "Create HealthRecordRepository with findByAnimalId",
    "Create HealthRecordService.registerHealthRecord() with animal existence check",
    "Create HealthRecordController POST /api/v1/animals/{animalId}/health",
    "Write Flyway migration V3__create_health_records.sql",
    "Write unit tests for HealthRecordService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthRecord",
    "service": "HealthRecordService",
    "repository": "HealthRecordRepository",
    "controller": "HealthRecordController",
    "dto_request": "HealthRecordCreateRequest",
    "dto_response": "HealthRecordResponse",
    "migration": "V3__create_health_records.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 2,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 2 - Health Management"
}

## Story 2: View animal health history
{
  "id": "STORY-009",
  "epic": "Animal Health",
  "title": "View animal health history",
  "description": "As a veterinarian, I want to view the complete health history of an animal so that I can understand its medical background.",
  "acceptance_criteria": [
    "Given valid animal ID, When GET /api/v1/animals/{animalId}/health is called, Then all health records for that animal are returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId}/health is called, Then 404 error is returned",
    "Given no health records exist, When GET /api/v1/animals/{animalId}/health is called, Then empty list is returned"
  ],
  "technical_tasks": [
    "Create HealthRecordController GET /api/v1/animals/{animalId}/health",
    "Create HealthRecordService.getHealthHistoryByAnimalId() method",
    "Write unit tests for HealthRecordService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthRecord",
    "service": "HealthRecordService",
    "repository": "HealthRecordRepository",
    "controller": "HealthRecordController",
    "dto_request": "",
    "dto_response": "HealthRecordListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 2,
  "dependencies": ["STORY-001", "STORY-008"],
  "phase": "Phase 2 - Health Management"
}

## Story 3: Update animal health record
{
  "id": "STORY-010",
  "epic": "Animal Health",
  "title": "Update animal health record",
  "description": "As a veterinarian, I want to update an existing health record so that information stays current.",
  "acceptance_criteria": [
    "Given valid health record ID and update data, When PUT /api/v1/health/{id} is called, Then health record is updated",
    "Given invalid health record ID, When PUT /api/v1/health/{id} is called, Then 404 error is returned",
    "Given invalid field values, When PUT /api/v1/health/{id} is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create HealthRecordController PUT /api/v1/health/{id}",
    "Create HealthRecordService.updateHealthRecord() method",
    "Update HealthRecord entity with all fields",
    "Write unit tests for HealthRecordService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthRecord",
    "service": "HealthRecordService",
    "repository": "HealthRecordRepository",
    "controller": "HealthRecordController",
    "dto_request": "HealthRecordUpdateRequest",
    "dto_response": "HealthRecordResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 2,
  "dependencies": ["STORY-008", "STORY-009"],
  "phase": "Phase 2 - Health Management"
}

## Story 4: Add health checkup schedule
{
  "id": "STORY-011",
  "epic": "Animal Health",
  "title": "Add health checkup schedule",
  "description": "As a farm manager, I want to schedule regular health checkups for animals so that preventive care is maintained.",
  "acceptance_criteria": [
    "Given animal ID and scheduled date, When POST /api/v1/animals/{animalId}/health/schedule is called, Then schedule is created and 201 returned",
    "Given invalid animal ID, When POST /api/v1/animals/{animalId}/health/schedule is called, Then 404 error is returned",
    "Given invalid date format, When POST /api/v1/animals/{animalId}/health/schedule is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create HealthSchedule JPA entity with schedule fields",
    "Create HealthScheduleRepository with findByAnimalId and date range queries",
    "Create HealthScheduleService.scheduleCheckup() method",
    "Create HealthScheduleController POST /api/v1/animals/{animalId}/health/schedule",
    "Write Flyway migration V4__create_health_schedules.sql",
    "Write unit tests for HealthScheduleService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthSchedule",
    "service": "HealthScheduleService",
    "repository": "HealthScheduleRepository",
    "controller": "HealthScheduleController",
    "dto_request": "HealthScheduleCreateRequest",
    "dto_response": "HealthScheduleResponse",
    "migration": "V4__create_health_schedules.sql"
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 2,
  "dependencies": ["STORY-001", "STORY-008"],
  "phase": "Phase 2 - Health Management"
}

## Story 5: View upcoming health schedules
{
  "id": "STORY-012",
  "epic": "Animal Health",
  "title": "View upcoming health schedules",
  "description": "As a farm worker, I want to view upcoming scheduled checkups so that I can prepare for them in advance.",
  "acceptance_criteria": [
    "Given date range parameters, When GET /api/v1/health/schedules is called, Then upcoming schedules are returned",
    "Given no schedules exist, When GET /api/v1/health/schedules is called, Then empty list is returned",
    "Given invalid date range, When GET /api/v1/health/schedules is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create HealthScheduleController GET /api/v1/health/schedules",
    "Create HealthScheduleService.getUpcomingSchedules() method",
    "Write unit tests for HealthScheduleService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthSchedule",
    "service": "HealthScheduleService",
    "repository": "HealthScheduleRepository",
    "controller": "HealthScheduleController",
    "dto_request": "",
    "dto_response": "HealthScheduleListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 2,
  "dependencies": ["STORY-011"],
  "phase": "Phase 2 - Health Management"
}

## Story 6: Mark health checkup as completed
{
  "id": "STORY-013",
  "epic": "Animal Health",
  "title": "Mark health checkup as completed",
  "description": "As a veterinarian, I want to mark a scheduled checkup as completed so that the system reflects actual care provided.",
  "acceptance_criteria": [
    "Given valid schedule ID, When PATCH /api/v1/health/schedules/{id}/complete is called, Then schedule status is updated to COMPLETED",
    "Given invalid schedule ID, When PATCH /api/v1/health/schedules/{id}/complete is called, Then 404 error is returned",
    "Given already completed schedule, When PATCH /api/v1/health/schedules/{id}/complete is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create HealthScheduleController PATCH /api/v1/health/schedules/{id}/complete",
    "Create HealthScheduleService.markCompleted() method",
    "Add business logic to prevent double completion",
    "Write unit tests for HealthScheduleService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "HealthSchedule",
    "service": "HealthScheduleService",
    "repository": "HealthScheduleRepository",
    "controller": "HealthScheduleController",
    "dto_request": "",
    "dto_response": "HealthScheduleResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 3,
  "sprint": 2,
  "dependencies": ["STORY-011", "STORY-012"],
  "phase": "Phase 2 - Health Management"
}