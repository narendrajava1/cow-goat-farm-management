# Phase 5 - Feed and Inventory

## Story 1: Register feed inventory
{
  "id": "STORY-026",
  "epic": "Feed Management",
  "title": "Register feed inventory",
  "description": "As a farm manager, I want to register new feed items in the inventory so that I can track what's available.",
  "acceptance_criteria": [
    "Given valid feed details, When POST /api/v1/feed/inventory is called, Then feed item is created and 201 returned",
    "Given duplicate feed name, When POST /api/v1/feed/inventory is called, Then 422 error is returned",
    "Given invalid feed data, When POST /api/v1/feed/inventory is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create FeedItem JPA entity with all feed fields (name, quantity, unit, supplier)",
    "Create FeedItemRepository with findByFeedName",
    "Create FeedItemService.registerFeed() method",
    "Create FeedItemController POST /api/v1/feed/inventory",
    "Write Flyway migration V10__create_feed_inventory.sql",
    "Write unit tests for FeedItemService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedItem",
    "service": "FeedItemService",
    "repository": "FeedItemRepository",
    "controller": "FeedItemController",
    "dto_request": "FeedItemCreateRequest",
    "dto_response": "FeedItemResponse",
    "migration": "V10__create_feed_inventory.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 5,
  "dependencies": [],
  "phase": "Phase 5 - Feed and Inventory"
}

## Story 2: View feed inventory
{
  "id": "STORY-027",
  "epic": "Feed Management",
  "title": "View feed inventory",
  "description": "As a farm worker, I want to view the current feed inventory so that I can see what's available and manage stock levels.",
  "acceptance_criteria": [
    "Given no filters, When GET /api/v1/feed/inventory is called, Then all inventory items are returned",
    "Given search parameter, When GET /api/v1/feed/inventory is called with search, Then filtered results are returned",
    "Given pagination parameters, When GET /api/v1/feed/inventory is called, Then paginated response is returned"
  ],
  "technical_tasks": [
    "Create FeedItemController GET /api/v1/feed/inventory with query params",
    "Create FeedItemService.getInventory() method with search and pagination",
    "Write unit tests for FeedItemService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedItem",
    "service": "FeedItemService",
    "repository": "FeedItemRepository",
    "controller": "FeedItemController",
    "dto_request": "",
    "dto_response": "FeedItemListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 5,
  "dependencies": ["STORY-026"],
  "phase": "Phase 5 - Feed and Inventory"
}

## Story 3: Update feed inventory
{
  "id": "STORY-028",
  "epic": "Feed Management",
  "title": "Update feed inventory",
  "description": "As a farm manager, I want to update feed quantities and details so that the inventory stays current.",
  "acceptance_criteria": [
    "Given valid feed ID and update data, When PUT /api/v1/feed/inventory/{id} is called, Then feed item is updated",
    "Given invalid feed ID, When PUT /api/v1/feed/inventory/{id} is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create FeedItemController PUT /api/v1/feed/inventory/{id}",
    "Create FeedItemService.updateFeed() method",
    "Update FeedItem entity with all fields",
    "Write unit tests for FeedItemService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedItem",
    "service": "FeedItemService",
    "repository": "FeedItemRepository",
    "controller": "FeedItemController",
    "dto_request": "FeedItemUpdateRequest",
    "dto_response": "FeedItemResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 5,
  "dependencies": ["STORY-026", "STORY-027"],
  "phase": "Phase 5 - Feed and Inventory"
}

## Story 4: Record feed consumption
{
  "id": "STORY-029",
  "epic": "Feed Management",
  "title": "Record feed consumption",
  "description": "As a farm worker, I want to record feed consumption for animals or herds so that feed distribution can be tracked.",
  "acceptance_criteria": [
    "Given valid animal/animal group and feed consumption data, When POST /api/v1/feed/consumption is called, Then consumption record is created and 201 returned",
    "Given invalid consumption data, When POST /api/v1/feed/consumption is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create FeedConsumption JPA entity with animal/animal group, feed item, and quantity fields",
    "Create FeedConsumptionRepository with findByAnimalId",
    "Create FeedConsumptionService.recordConsumption() method",
    "Create FeedConsumptionController POST /api/v1/feed/consumption",
    "Write Flyway migration V11__create_feed_consumption.sql",
    "Write unit tests for FeedConsumptionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedConsumption",
    "service": "FeedConsumptionService",
    "repository": "FeedConsumptionRepository",
    "controller": "FeedConsumptionController",
    "dto_request": "FeedConsumptionCreateRequest",
    "dto_response": "FeedConsumptionResponse",
    "migration": "V11__create_feed_consumption.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 5,
  "dependencies": ["STORY-026", "STORY-027"],
  "phase": "Phase 5 - Feed and Inventory"
}

## Story 5: View feed consumption history
{
  "id": "STORY-030",
  "epic": "Feed Management",
  "title": "View feed consumption history",
  "description": "As a farm manager, I want to view historical feed consumption so that I can understand feeding patterns and optimize.",
  "acceptance_criteria": [
    "Given date range parameters, When GET /api/v1/feed/consumption/history is called, Then consumption records are returned",
    "Given animal ID, When GET /api/v1/animals/{animalId}/feed/consumption is called, Then animal's consumption history is returned"
  ],
  "technical_tasks": [
    "Create FeedConsumptionController GET /api/v1/feed/consumption/history and GET /api/v1/animals/{animalId}/feed/consumption",
    "Create FeedConsumptionService.getConsumptionHistory() method",
    "Write unit tests for FeedConsumptionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedConsumption",
    "service": "FeedConsumptionService",
    "repository": "FeedConsumptionRepository",
    "controller": "FeedConsumptionController",
    "dto_request": "",
    "dto_response": "FeedConsumptionListResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 5,
  "dependencies": ["STORY-029"],
  "phase": "Phase 5 - Feed and Inventory"
}

## Story 6: Generate feed reports
{
  "id": "STORY-031",
  "epic": "Feed Management",
  "title": "Generate feed reports",
  "description": "As a farm manager, I want to generate reports on feed usage and inventory so that I can make informed decisions.",
  "acceptance_criteria": [
    "Given date range and filters, When GET /api/v1/feed/reports is called, Then generated report is returned",
    "Given invalid parameters, When GET /api/v1/feed/reports is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create FeedReportController GET /api/v1/feed/reports",
    "Create FeedReportService.generateReport() method with aggregation logic",
    "Add report generation functionality in repository",
    "Write unit tests for FeedReportService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "FeedConsumption, FeedItem",
    "service": "FeedReportService",
    "repository": "FeedConsumptionRepository, FeedItemRepository",
    "controller": "FeedReportController",
    "dto_request": "",
    "dto_response": "FeedReportResponse",
    "migration": ""
  },
  "priority": "MEDIUM",
  "story_points": 5,
  "sprint": 5,
  "dependencies": ["STORY-026", "STORY-027", "STORY-029", "STORY-030"],
  "phase": "Phase 5 - Feed and Inventory"
}