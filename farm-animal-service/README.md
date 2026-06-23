# Farm Animal Service

This service handles animal and herd management functionality for the farm management system.

## Features Implemented (Phase 1 - Animal Registry)

- Register new animals with tag numbers
- View animal details
- List animals with filters and pagination
- Update animal details
- Change animal status
- Manage herds of animals
- View animals in specific herds

## API Endpoints

### Animals
- `POST /api/v1/animals` - Register a new animal
- `GET /api/v1/animals/{id}` - Get animal by ID
- `GET /api/v1/animals` - List animals with filters
- `PUT /api/v1/animals/{id}` - Update animal details
- `PATCH /api/v1/animals/{id}/status` - Change animal status

### Herds
- `POST /api/v1/herds` - Create a new herd
- `GET /api/v1/herds/{id}` - Get herd by ID
- `PUT /api/v1/herds/{id}` - Update herd details
- `DELETE /api/v1/herds/{id}` - Delete a herd
- `GET /api/v1/herds/{id}/animals` - Get animals in a herd

## Database Schema

The service uses PostgreSQL with Liquibase for database migrations:
- Animals table with tag number uniqueness constraint
- Herds table for grouping animals