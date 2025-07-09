# NotesAPI

A RESTful API for managing personal notes built with Spring Boot and PostgreSQL.

## Prerequisites

- Java 21 or higher
- PostgreSQL 15 or higher
- Maven 3.x

## Quick Start

1. Clone the repository
2. Configure PostgreSQL
    - Create a database
    - Update `src/main/resources/application.yml` with your database credentials
3. Build and run the application
    -  `./mvnw spring-boot:run`

The API will be available at `http://localhost:8080`

## API Documentation

### Authentication Endpoints
https://web.postman.co/workspace/My-Workspace~e73e4812-3f0b-42f3-806c-904e9cc6728e/collection/16328748-4d2ee3f5-bb53-4b2b-8342-09bb2a86ad60?action=share&source=copy-link&creator=16328748

### Notes Endpoints
https://web.postman.co/workspace/My-Workspace~e73e4812-3f0b-42f3-806c-904e9cc6728e/folder/16328748-e8fb3516-f6c8-4251-84b2-ab30f1ff52e5?action=share&source=copy-link&creator=16328748&ctx=documentation

## Build and Deploy

### Docker
- Build Image
- `./mvnw spring-boot:build-image -Dspring-boot.build-image.imagePlatform=linux/amd64 -Dspring-boot.build-image.imageName=notesapi:latest`
- `docker tag notesapi:latest ghcr.io/kirilkv/notesapi:0.0.1-snapshot`
- `docker push ghcr.io/kirilkv/notesapi:0.0.1-snapshot`


