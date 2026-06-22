# Phase 6 - Financial Management

## Story 1: Register animal purchase transaction
{
  "id": "STORY-032",
  "epic": "Financial Transactions",
  "title": "Register animal purchase transaction",
  "description": "As a farm manager, I want to register animal purchase transactions so that all financial records are properly documented.",
  "acceptance_criteria": [
    "Given valid animal ID and purchase details, When POST /api/v1/transactions/purchases is called, Then purchase transaction is created and 201 returned",
    "Given invalid animal ID, When POST /api/v1/transactions/purchases is called, Then 404 error is returned",
    "Given missing required fields, When POST /api/v1/transactions/purchases is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create PurchaseTransaction JPA entity with all purchase fields (price, supplier, date)",
    "Create PurchaseTransactionRepository with findByAnimalId",
    "Create PurchaseTransactionService.registerPurchase() method",
    "Create PurchaseTransactionController POST /api/v1/transactions/purchases",
    "Write Liquibase migration V12__create_purchase_transactions.sql",
    "Write unit tests for PurchaseTransactionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "PurchaseTransaction",
    "service": "PurchaseTransactionService",
    "repository": "PurchaseTransactionRepository",
    "controller": "PurchaseTransactionController",
    "dto_request": "PurchaseTransactionCreateRequest",
    "dto_response": "PurchaseTransactionResponse",
    "migration": "V12__create_purchase_transactions.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 6,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 6 - Financial Management"
}

## Story 2: Register animal sale transaction
{
  "id": "STORY-033",
  "epic": "Financial Transactions",
  "title": "Register animal sale transaction",
  "description": "As a farm manager, I want to register animal sale transactions so that all revenue is properly tracked.",
  "acceptance_criteria": [
    "Given valid animal ID and sale details, When POST /api/v1/transactions/sales is called, Then sale transaction is created and 201 returned",
    "Given invalid animal ID, When POST /api/v1/transactions/sales is called, Then 404 error is returned",
    "Given missing required fields, When POST /api/v1/transactions/sales is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create SaleTransaction JPA entity with all sale fields (price, buyer, date)",
    "Create SaleTransactionRepository with findByAnimalId",
    "Create SaleTransactionService.registerSale() method",
    "Create SaleTransactionController POST /api/v1/transactions/sales",
    "Write Liquibase migration V13__create_sale_transactions.sql",
    "Write unit tests for SaleTransactionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "SaleTransaction",
    "service": "SaleTransactionService",
    "repository": "SaleTransactionRepository",
    "controller": "SaleTransactionController",
    "dto_request": "SaleTransactionCreateRequest",
    "dto_response": "SaleTransactionResponse",
    "migration": "V13__create_sale_transactions.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 6,
  "dependencies": ["STORY-001", "STORY-002"],
  "phase": "Phase 6 - Financial Management"
}

## Story 3: View animal financial history
{
  "id": "STORY-034",
  "epic": "Financial Transactions",
  "title": "View animal financial history",
  "description": "As a farm manager, I want to view the complete financial history of an animal so that I can track costs and earnings.",
  "acceptance_criteria": [
    "Given valid animal ID, When GET /api/v1/animals/{animalId}/financial/history is called, Then all transactions for that animal are returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId}/financial/history is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create FinancialTransactionController GET /api/v1/animals/{animalId}/financial/history",
    "Create FinancialTransactionService.getAnimalFinancialHistory() method",
    "Write unit tests for FinancialTransactionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "PurchaseTransaction, SaleTransaction",
    "service": "FinancialTransactionService",
    "repository": "PurchaseTransactionRepository, SaleTransactionRepository",
    "controller": "FinancialTransactionController",
    "dto_request": "",
    "dto_response": "FinancialHistoryResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 3,
  "sprint": 6,
  "dependencies": ["STORY-032", "STORY-033"],
  "phase": "Phase 6 - Financial Management"
}

## Story 4: Calculate animal profitability
{
  "id": "STORY-035",
  "epic": "Financial Transactions",
  "title": "Calculate animal profitability",
  "description": "As a farm manager, I want to calculate the profitability of individual animals so that I can assess performance.",
  "acceptance_criteria": [
    "Given animal ID, When GET /api/v1/animals/{animalId}/profitability is called, Then calculated profitability metrics are returned",
    "Given invalid animal ID, When GET /api/v1/animals/{animalId}/profitability is called, Then 404 error is returned"
  ],
  "technical_tasks": [
    "Create FinancialTransactionController GET /api/v1/animals/{animalId}/profitability",
    "Create FinancialTransactionService.calculateProfitability() method",
    "Add calculation logic in repository for profit calculations",
    "Write unit tests for FinancialTransactionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "PurchaseTransaction, SaleTransaction",
    "service": "FinancialTransactionService",
    "repository": "PurchaseTransactionRepository, SaleTransactionRepository",
    "controller": "FinancialTransactionController",
    "dto_request": "",
    "dto_response": "ProfitabilityResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 6,
  "dependencies": ["STORY-032", "STORY-033", "STORY-034"],
  "phase": "Phase 6 - Financial Management"
}

## Story 5: Register operational expenses
{
  "id": "STORY-036",
  "epic": "Financial Transactions",
  "title": "Register operational expenses",
  "description": "As a farm manager, I want to register operational expenses so that all costs are properly tracked.",
  "acceptance_criteria": [
    "Given valid expense details, When POST /api/v1/transactions/expenses is called, Then expense transaction is created and 201 returned",
    "Given invalid expense data, When POST /api/v1/transactions/expenses is called, Then validation errors are returned"
  ],
  "technical_tasks": [
    "Create ExpenseTransaction JPA entity with all expense fields (category, amount, date)",
    "Create ExpenseTransactionRepository",
    "Create ExpenseTransactionService.registerExpense() method",
    "Create ExpenseTransactionController POST /api/v1/transactions/expenses",
    "Write Liquibase migration V14__create_expense_transactions.sql",
    "Write unit tests for ExpenseTransactionService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "ExpenseTransaction",
    "service": "ExpenseTransactionService",
    "repository": "ExpenseTransactionRepository",
    "controller": "ExpenseTransactionController",
    "dto_request": "ExpenseTransactionCreateRequest",
    "dto_response": "ExpenseTransactionResponse",
    "migration": "V14__create_expense_transactions.sql"
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 6,
  "dependencies": [],
  "phase": "Phase 6 - Financial Management"
}

## Story 6: Generate financial reports
{
  "id": "STORY-037",
  "epic": "Financial Transactions",
  "title": "Generate financial reports",
  "description": "As a farm manager, I want to generate financial reports so that I can understand the overall financial health of the farm.",
  "acceptance_criteria": [
    "Given date range and report type, When GET /api/v1/financial/reports is called, Then generated report is returned",
    "Given invalid parameters, When GET /api/v1/financial/reports is called, Then 422 error is returned"
  ],
  "technical_tasks": [
    "Create FinancialReportController GET /api/v1/financial/reports",
    "Create FinancialReportService.generateReport() method with various report types",
    "Add aggregation functions for financial calculations",
    "Write unit tests for FinancialReportService",
    "Write integration test with TestContainers"
  ],
  "spring_components": {
    "entity": "PurchaseTransaction, SaleTransaction, ExpenseTransaction",
    "service": "FinancialReportService",
    "repository": "PurchaseTransactionRepository, SaleTransactionRepository, ExpenseTransactionRepository",
    "controller": "FinancialReportController",
    "dto_request": "",
    "dto_response": "FinancialReportResponse",
    "migration": ""
  },
  "priority": "HIGH",
  "story_points": 5,
  "sprint": 6,
  "dependencies": ["STORY-032", "STORY-033", "STORY-034", "STORY-035", "STORY-036"],
  "phase": "Phase 6 - Financial Management"
}