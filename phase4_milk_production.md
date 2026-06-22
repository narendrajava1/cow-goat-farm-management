# Phase 4 - Milk Production

## Story 1: Register milk production record
{
  "id": "STORY-020",
  "epic": "Milk Production Management",
  "title": "Register milk production record",
  "description": "As a farm worker, I want to register daily milk production for animals so that production data is documented.",
  "acceptance_criteria": [
    "Given valid animal ID and production data, When POST /api/v1/milk/production is called, Then production record is created and 201 returned",
    "Given invalid animal ID, When POST /api/v1/milk/production is called, Then 404 error is returned",
    "Given invalid production data, When POST /api/v1/milk/production is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create MilkProduction JPA entity with quantity and date fields",
    "Create MilkProductionRepository with findByAnimalIdAndDate",
    "Create MilkProductionService.registerProduction() method",
    "Create MilkProductionController POST /api/v1/milk/production",
    "Write Flyway migration V8__create_milk_production.sql",
    "Write unit tests for MilkProductionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkProduction",
    "service": "MilkProductionService",
    "repository": "MilkProductionRepository",
    "controller": "MilkProductionController",
    "dto_request": "MilkProductionCreateRequest",
    "dto_response": "MilkProductionResponse",
    "migration": "V8__create_milk_production.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 4,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 4 - Milk Production"
}

## Story 2: View daily milk production
{
  "id": "STORY-021",
  "epic": "Milk Production Management",
  "title": "View daily milk production",
  "description": "As a farm manager, I want to view daily milk production records so that I can monitor productivity.",
  "acceptance_criteria": [
    "Given valid date, When GET /api/v1/milk/production/daily is called, Then all production for that day is returned",
    "Given invalid date format, When GET /api/v1/milk/production/daily is called, Then 422 error is returned",
    "Given no records exist for date, When GET /api/v1/milk/production/daily is called, Then empty list is returned"
  ],
  "technical_tasks": [
    "Create MilkProductionController GET /api/v1/milk/production/daily",
    "Create MilkProductionService.getDailyProduction() method",
    "Write unit tests for MilkProductionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkProduction",
    "service": "MilkProductionService",
    "repository": "MilkProductionRepository",
    "controller": "MilkProductionController",
    "dto_request": "",
    "dto_response": "MilkProductionDailyResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 4,
  "dependencies": ["STORY-020"],
  "phase": "Phase 4 - Milk Production"
}

## Story 3: View animal milk history
{
  "id": "STORY-022",
  "epic": "Milk Production Management",
  "title": "View animal milk history",
  "description": "As a farm worker, I want to view the complete milk production history of an animal so that I can track individual performance.",
  "acceptance_criteria": [
    "Given valid animal ID, When GET /api/v1/animals/{animalId}/milk/history is called, Then all production records for that animal are returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId}/milk/history is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create MilkProductionController GET /api/v1/animals/{animalId}/milk/history",
    "Create MilkProductionService.getAnimalMilkHistory() method",
    "Write unit tests for MilkProductionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkProduction",
    "service": "MilkProductionService",
    "repository": "MilkProductionRepository",
    "controller": "MilkProductionController",
    "dto_request": "",
    "dto_response": "MilkProductionListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 4,
  "dependencies": ["STORY-020", "STORY-021"],
  "phase": "Phase 4 - Milk Production"
}

## Story 4: Calculate milk production statistics
{
  "id": "STORY-023",
  "epic": "Milk Production Management",
  "title": "Calculate milk production statistics",
  "description": "As a farm manager, I want to view milk production statistics so that I can assess overall herd performance.",
  "acceptance_criteria": [
    "Given date range parameters, When GET /api/v1/milk/statistics is called, Then summary statistics are returned",
    "Given invalid date range, When GET /api/v1/milk/statistics is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create MilkProductionController GET /api/v1/milk/statistics",
    "Create MilkProductionService.calculateStatistics() method",
    "Add aggregation functions in repository to calculate statistics",
    "Write unit tests for MilkProductionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkProduction",
    "service": "MilkProductionService",
    "repository": "MilkProductionRepository",
    "controller": "MilkProductionController",
    "dto_request": "",
    "dto_response": "MilkProductionStatsResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 4,
  "dependencies": ["STORY-020", "STORY-021"],
  "phase": "Phase 4 - Milk Production"
}

## Story 5: Update milk production record
{
  "id": "STORY-024",
  "epic": "Milk Production Management",
  "title": "Update milk production record",
  "description": "As a farm worker, I want to update previous milk production records so that data stays accurate.",
  "acceptance_criteria": [
    "Given valid record ID and updated data, When PUT /api/v1/milk/production/{id} is called, Then record is updated",
    "Given invalid record ID, When PUT /api/v1/milk/production/{id} is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create MilkProductionController PUT /api/v1/milk/production/{id}",
    "Create MilkProductionService.updateProduction() method",
    "Update MilkProduction entity with all fields",
    "Write unit tests for MilkProductionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkProduction",
    "service": "MilkProductionService",
    "repository": "MilkProductionRepository",
    "controller": "MilkProductionController",
    "dto_request": "MilkProductionUpdateRequest",
    "dto_response": "MilkProductionResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 4,
  "dependencies": ["STORY-020", "STORY-021", "STORY-022"],
  "phase": "Phase 4 - Milk Production"
}

## Story 6: Set milk production targets
{
  "id": "STORY-025",
  "epic": "Milk Production Management",
  "title": "Set milk production targets",
  "description": "As a farm manager, I want to set production targets for animals or herds so that productivity can be tracked against goals.",
  "acceptance_criteria": [
    "Given animal ID and target values, When POST /api/v1/milk/targets is called, Then target is created and 201 returned",
    "Given valid target ID, When GET /api/v1/milk/targets/{id} is called, Then target details are returned"
  ],
  "technical_tasks": [
    "Create MilkTarget JPA entity with target fields",
    "Create MilkTargetRepository with findByAnimalId and herdId",
    "Create MilkTargetService.setTarget() method",
    "Create MilkTargetController POST /api/v1/milk/targets",
    "Write Flyway migration V9__create_milk_targets.sql",
    "Write unit tests for MilkTargetService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "MilkTarget",
    "service": "MilkTargetService",
    "repository": "MilkTargetRepository",
    "controller": "MilkTargetController",
    "dto_request": "MilkTargetCreateRequest",
    "dto_response": "MilkTargetResponse",
    "migration": "V9__create_milk_targets.sql"
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 4,
  "dependencies": ["STORY-020", "STORY-023"],
  "phase": "Phase 4 - Milk Production"
}