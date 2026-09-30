# LEKANG JOURNAL API

Java 21 / Spring Boot 3.5 modular monolith for the LEKANG JOURNAL public content API.

## Requirements

- JDK 21
- PostgreSQL
- Docker Desktop only when running Testcontainers integration tests

## Local configuration

Set environment variables instead of committing credentials:

```text
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5432/postgres?currentSchema=lekang_journal
DB_USERNAME=postgres
DB_PASSWORD=<local password>
```

The local defaults target the dedicated `lekang_journal` schema in the `postgres` database. The
application binds to `127.0.0.1:8081`. Flyway creates and seeds its tables in the configured schema;
Hibernate only validates the migrated schema. Keep `DB_PASSWORD` in the process environment and
never commit it.

## Build

```text
# Windows
mvnw.cmd test
mvnw.cmd verify

# Linux/macOS
./mvnw test
./mvnw verify
```

## Public endpoints

- `GET /api/v1/home`
- `GET /api/v1/articles`
- `GET /api/v1/articles/{slug}`
- `GET /api/v1/articles/{slug}/navigation`
- `GET /api/v1/taxonomies`
- `GET /api/v1/archives`
- `GET /api/v1/search?q=...`
- `GET /api/v1/albums`
- `GET /api/v1/albums/{slug}`
- `GET /api/v1/albums/{slug}/photos`
- `GET /api/v1/profile`
- `GET /actuator/health/readiness`

Local OpenAPI JSON is available at `/api/v1/openapi`; Swagger UI is disabled in `prod`.

Management authentication and article writing APIs are implemented under `/api/v1/admin` for
local development. They use a server-side Session, CSRF protection, BCrypt credentials, optimistic
locking, Markdown rendering/sanitization, and audit records. Create the first local administrator
only through the runtime-only `JOURNAL_ADMIN_USERNAME` and `JOURNAL_ADMIN_PASSWORD` bootstrap
variables; never commit credentials. Production `/admin` remains disabled operationally until
HTTPS, Secure Cookie, backup, and rollback checks are complete. Cover media is available through
`GET/POST /api/v1/admin/media` and public `GET /api/v1/media/{id}/content`. Upload accepts re-encoded
JPEG/PNG images up to 8 MB and stores bytes outside PostgreSQL under
`JOURNAL_MEDIA_ROOT` (default `./data/media`). Configure an absolute persistent directory in production;
S3/R2 and CDN delivery remain a later storage-adapter change.
