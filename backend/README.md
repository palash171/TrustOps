# TrustOps Backend

TrustOps is a comment moderation backend for communities that need to review user-generated content safely and quickly.

The goal is simple: accept a comment, store it durably, let moderators review it later, and keep a clear record of the moderation status.

## Why this project exists

Online communities get more comments than humans can review manually. TrustOps is built to show how a moderation system can:

- accept incoming comments through an HTTP API
- store them in PostgreSQL instead of memory
- keep moderation decisions explicit
- support filtering by moderation status
- keep database changes tracked with Flyway

This backend is the foundation of the product. A future frontend can sit on top of it and show the review queue, filters, and moderator actions.

## What is implemented now

- Create comments
- List comments
- Filter comments by moderation status
- Update comment status
- Persist comments in PostgreSQL
- Manage schema changes with Flyway migrations
- Validate the Java model against the database schema at startup
- Keep the backend configuration environment-friendly for local and deployed runs

## How the backend works

```text
HTTP request
→ CommentWebApi
→ CommentManger
→ CommentStore
→ PostgreSQL
```

- `CommentWebApi` receives HTTP requests and turns them into Java calls.
- `CommentManger` contains the comment workflow and business rules.
- `CommentStore` is the Spring Data repository that talks to the database.
- PostgreSQL stores the real data.

## Data model

Each comment has:

- `id`
- `text`
- `status`
- `receivedAt`

The database table is created by the Flyway migration in:

```text
src/main/resources/db/migration/V1__create_comments_table.sql
```

## Key project files

- `src/main/java/com/trustops/backend/TrustopsBackendApplication.java` starts the Spring Boot app.
- `src/main/java/com/trustops/backend/comment/CommentWebApi.java` handles HTTP requests.
- `src/main/java/com/trustops/backend/comment/CommentManger.java` contains the comment logic.
- `src/main/java/com/trustops/backend/comment/CommentStore.java` is the repository interface.
- `src/main/java/com/trustops/backend/comment/Comment.java` defines the comment entity.
- `src/main/resources/application.properties` configures the database and JPA behavior.
- `src/main/resources/db/migration/V1__create_comments_table.sql` creates the comments table.
- `src/test/java/com/trustops/backend/comment/CommentMangerTests.java` covers the moderation logic.

## Local setup

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run the backend

```bash
./mvnw spring-boot:run
```

### 3. Run the tests

```bash
./mvnw test
```

## API

### Create a comment

```bash
curl -X POST "http://localhost:8080/api/v1/comments" \
  -H "Content-Type: application/json" \
  -d '{"text":"Hello world"}'
```

### List all comments

```bash
curl "http://localhost:8080/api/v1/comments"
```

### Filter by status

```bash
curl "http://localhost:8080/api/v1/comments?status=PENDING"
```

### Update moderation status

```bash
curl -X PATCH "http://localhost:8080/api/v1/comments/{id}/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"APPROVED"}'
```

## Database setup

Spring Boot reads connection settings from environment variables first:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

If those are not set, it falls back to local defaults for development.

Flyway runs the SQL migration files at startup. Hibernate is set to `validate`, which means it checks that the Java entity and the database table match instead of changing the schema automatically.

## Current scope

This repository is intentionally focused on the backend core:

- HTTP API
- moderation logic
- persistence
- schema migrations
- tests

The frontend dashboard is the next layer on top of this backend.

## Tech stack

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker Compose
- JUnit 5
- Mockito

