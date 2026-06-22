# Phase 1 - Animal Registry - Sprint Ready User Stories

## Story 1: Register a new animal with tag number
{
  "id": "STORY-001",
  "epic": "Animal Management",
  "title": "Register a new animal with tag number",
  "description": "As a farm worker, I want to register a new cow or goat so that it appears in the farm registry.",
  "acceptance_criteria": [
    "Given a valid tag number, When POST /api/v1/animals is called, Then animal is saved and 201 returned",
    "Given a duplicate tag number, When POST /api/v1/animals is called, Then 422 error is returned",
    "Given missing required fields, When POST /api/v1/animals is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create Animal JPA entity with all fields from domain model",
    "Create AnimalRepository with findByFarmIdAndTagNumber",
    "Create AnimalService.registerAnimal() with duplicate check",
    "Create AnimalController POST /api/v1/animals",
    "Write Liquibase migration V1__create_animals.sql",
    "Write unit tests for AnimalService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "AnimalService",
    "repository": "AnimalRepository",
    "controller": "AnimalController",
    "dto_request": "AnimalCreateRequest",
    "dto_response": "AnimalResponse",
    "migration": "V1__create_animals.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 1,
  "dependencies": [],
  "phase": "Phase 1 - Animal Registry"
}

## Story 2: View animal profile details
{
  "id": "STORY-002",
  "epic": "Animal Management",
  "title": "View animal profile details",
  "description": "As a farm worker, I want to view all details of an individual animal so that I can access their complete information.",
  "acceptance_criteria": [
    "Given a valid animal ID, When GET /api/v1/animals/{animalId} is called, Then full animal profile is returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId} is called, Then 404 error is returned",
    "Given animal exists but not in farm scope, When GET /api/v1/animals/{animalId} is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create AnimalController GET /api/v1/animals/{id}",
    "Create AnimalService.getAnimalById()",
    "Create AnimalResponse DTO with computed fields (age, lactation status)",
    "Write unit tests for AnimalService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "AnimalService",
    "repository": "AnimalRepository",
    "controller": "AnimalController",
    "dto_request": "",
    "dto_response": "AnimalResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 1,
  "dependencies": ["STORY-001"],
  "phase": "Phase 1 - Animal Registry"
}

## Story 3: List animals with filters and pagination
{
  "id": "STORY-003",
  "epic": "Animal Management",
  "title": "List animals with filters and pagination",
  "description": "As a farm worker, I want to see a list of animals in the farm filtered by type, gender, or status so that I can quickly find specific animals.",
  "acceptance_criteria": [
    "Given filter parameters, When GET /api/v1/animals is called with query params, Then filtered results are returned",
    "Given pagination parameters, When GET /api/v1/animals is called with page and size, Then paginated response is returned",
    "Given no filters, When GET /api/v1/animals is called, Then all animals in farm are returned",
    "Given invalid filter values, When GET /api/v1/animals is called, Then 422 validation error is returned"
  ],
  "technical_tasks": [
    "Create AnimalController GET /api/v1/animals with query params",
    "Create AnimalRepository with custom query methods for filtering",
    "Create AnimalListResponse DTO for paginated results",
    "Implement pagination and sorting logic in AnimalService",
    "Write unit tests for AnimalService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "AnimalService",
    "repository": "AnimalRepository",
    "controller": "AnimalController",
    "dto_request": "",
    "dto_response": "AnimalListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 1,
  "dependencies": ["STORY-001"],
  "phase": "Phase 1 - Animal Registry"
}

## Story 4: Update animal details
{
  "id": "STORY-004",
  "epic": "Animal Management",
  "title": "Update animal details",
  "description": "As a farm worker, I want to update an animal's details like name, weight, color markings so that the registry stays current.",
  "acceptance_criteria": [
    "Given valid animal ID and partial data, When PUT /api/v1/animals/{id} is called, Then animal details are updated",
    "Given invalid animal ID, When PUT /api/v1/animals/{id} is called, Then 404 error is returned",
    "Given invalid field values, When PUT /api/v1/animals/{id} is called, Then validation errors are returned",
    "Given duplicate tag number in update, When PUT /api/v1/animals/{id} is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create AnimalController PUT /api/v1/animals/{id}",
    "Create AnimalService.updateAnimal() method",
    "Update Animal entity with all fields",
    "Add validation for tag uniqueness during update",
    "Write unit tests for AnimalService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "AnimalService",
    "repository": "AnimalRepository",
    "controller": "AnimalController",
    "dto_request": "AnimalUpdateRequest",
    "dto_response": "AnimalResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 1,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 1 - Animal Registry"
}

## Story 5: Change animal status
{
  "id": "STORY-005",
  "epic": "Animal Management",
  "title": "Change animal status",
  "description": "As a farm owner, I want to change an animal's status (ACTIVE, SOLD, DECEASED, QUARANTINED) so that the registry reflects current animal status.",
  "acceptance_criteria": [
    "Given valid animal ID and status, When PATCH /api/v1/animals/{id}/status is called, Then animal status is updated",
    "Given invalid animal ID, When PATCH /api/v1/animals/{id}/status is called, Then 404 error is returned",
    "Given invalid status value, When PATCH /api/v1/animals/{id}/status is called, Then 422 error is returned",
    "Given status transition is invalid, When PATCH /api/v1/animals/{id}/status is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create AnimalController PATCH /api/v1/animals/{id}/status",
    "Create AnimalService.changeAnimalStatus() method",
    "Add business rules validation for status transitions",
    "Write unit tests for AnimalService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "AnimalService",
    "repository": "AnimalRepository",
    "controller": "AnimalController",
    "dto_request": "AnimalStatusUpdateRequest",
    "dto_response": "AnimalResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 1,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 1 - Animal Registry"
}

## Story 6: Manage animal herds
{
  "id": "STORY-006",
  "epic": "Herd Management",
  "title": "Manage animal herds",
  "description": "As a farm manager, I want to create and organize animals into herds so that I can manage groups of animals for different purposes.",
  "acceptance_criteria": [
    "Given valid herd details, When POST /api/v1/herds is called, Then herd is created and 201 returned",
    "Given a valid herd ID, When GET /api/v1/herds/{id} is called, Then herd details are returned",
    "Given invalid herd ID, When GET /api/v1/herds/{id} is called, Then 404 error is returned",
    "Given valid herd ID and updated details, When PUT /api/v1/herds/{id} is called, Then herd details are updated",
    "Given herd with animals, When DELETE /api/v1/herds/{id} is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create Herd JPA entity",
    "Create HerdRepository",
    "Create HerdService with validation",
    "Create HerdController with all CRUD endpoints",
    "Write Liquibase migration V2__create_herds.sql",
    "Add business rule to prevent deletion of herds with animals",
    "Write unit tests for HerdService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Herd",
    "service": "HerdService",
    "repository": "HerdRepository",
    "controller": "HerdController",
    "dto_request": "HerdCreateRequest",
    "dto_response": "HerdResponse",
    "migration": "V2__create_herds.sql"
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 1,
  "dependencies": ["STORY-001"],
  "phase": "Phase 1 - Animal Registry"
}

## Story 7: View herd animal list
{
  "id": "STORY-007",
  "epic": "Herd Management",
  "title": "View herd animal list",
  "description": "As a farm worker, I want to see all animals in a specific herd so that I can get an overview of animals for that group.",
  "acceptance_criteria": [
    "Given valid herd ID, When GET /api/v1/herds/{id}/animals is called, Then list of animals in that herd is returned",
    "Given invalid herd ID, When GET /api/v1/herds/{id}/animals is called, Then 404 error is returned",
    "Given pagination parameters, When GET /api/v1/herds/{id}/animals is called, Then paginated response is returned"
  ],
  "technical_tasks": [
    "Create HerdController GET /api/v1/herds/{id}/animals",
    "Create HerdService.getAnimalsByHerdId() method",
    "Add herd ID to Animal entity and repository with query methods",
    "Write unit tests for HerdService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "Animal",
    "service": "HerdService",
    "repository": "AnimalRepository",
    "controller": "HerdController",
    "dto_request": "",
    "dto_response": "AnimalListResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 3,
  "sprint": 1,
  "dependencies": ["STORY-001", "STORY-006"],
  "phase": "Phase 1 - Animal Registry"
}
