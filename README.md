# CastQuest (formerly Cinephilia)

CastQuest is a Java application for movie and TV enthusiasts. The aim of the game is to connect two actors through
their movie and TV credits in as few steps as possible.

This is an evolving study project created by [Ana Maestrello](https://github.com/anacmm).

## Features

- Browse popular and top-rated movies and TV shows.
- Browse people and their movie and TV credits.
- Connect actors through their shared work.
- Fetch current data from [The Movie Database (TMDb)](https://www.themoviedb.org/).
- Explore and test the API through Swagger UI.

## Random popular person

`GET /person/random` returns one `Person` JSON object chosen uniformly from TMDB's top 250 popular
people. The backend fetches up to 13 pages, keeps the first 250 distinct person IDs in TMDB order,
and caches the pool in memory for one hour per application instance. The first request and the first
request after expiry refresh the pool. If TMDB returns fewer people, selection uses the available
pool; an empty pool returns HTTP 503. Failed upstream requests use the existing error handling and
are not cached. Consecutive calls may return the same person. Responses use `Cache-Control: no-store`
so clients request a fresh random selection each time. Rankings can change between refreshes.
Use `GET /person/{person-id}` for the selected person's full details.

## Cast and person credits

- `GET /movies/{movieId}/credits` returns movie cast (for example, `/movies/603/credits` for The Matrix).
- `GET /tv/{tvId}/credits` returns the latest season's TV cast (for example, `/tv/1396/credits` for Breaking Bad).
- `GET /person/{person-id}/credits` returns a person's combined filmography with `creditType` set to `MOVIE` or `TV`.

The cast endpoints return a JSON array using the existing `Cast` model, including `id` (TMDB person ID), `name`,
`character`, `profile_path`, and `order`. Other existing `Cast` fields may also appear. Optional values can be null;
no cast returns `[]`. Crew and the upstream credits wrapper are omitted. TMDB's cast order is preserved.

TV `/credits` covers the latest season, rather than all seasons; TMDB's separate `/aggregate_credits` API would be
needed for an all-season view. Both cast endpoints are documented in Swagger UI. Authentication stays server-side
through the existing Feign API-key interceptor. Feign request logging is disabled to keep credentials out of logs,
and upstream error responses omit request URLs.

### Cast and directors (backward compatible)

TMDB movie and TV `/credits` responses contain both `cast` and `crew`. Previously, our Feign
`CreditResponse` only modeled `cast`, so `crew` was discarded during deserialization. The existing public
`/credits` endpoints still return the same cast arrays. Changing them to objects would break consumers
that expect arrays; no frontend changes are required.

Use these additional endpoints for an object containing both cast and directors:

- `GET /movies/{movieId}/credits/details`
- `GET /tv/{tvId}/credits/details`

```json
{
  "cast": [{"id": 6384, "name": "Keanu Reeves", "character": "Neo", "profile_path": "/example.jpg", "order": 0}],
  "directors": [{"id": 100, "name": "Example director", "profile_path": null}]
}
```

The example is illustrative. `cast` contains exactly the same entries, fields, null values and order as
its cast-only endpoint. `directors` includes every returned crew entry whose `job` is exactly `Director`,
with only person `id`, `name` and `profile_path`. Assistant directors, producers and other jobs are excluded.
Missing, null or empty crew produces `directors: []`. Profile paths may be null. Each request uses one existing
TMDB credits call and the same authentication and error handling. Swagger documents the additional endpoints.

TV directors are **not overall series directors**. Different episodes and seasons can have different directors,
and a show may return several directors or none. We filter the `job` field of the crew returned by the existing
[TMDB TV credits endpoint](https://developer.themoviedb.org/reference/tv-series-credits), which covers the latest
season. This is not a complete list across all episodes, and an empty list does not prove the show has no directors.
We do not infer directors from creators, producers, the Directing department, or cast membership.
TMDB's [aggregate credits](https://developer.themoviedb.org/reference/tv-series-aggregate-credits) cover all episodes
and represent crew roles in `jobs[]` with episode counts, rather than the flat `job` used here. An all-season or
episode-specific view would require separate integration and is not implemented by these endpoints.

Automated cast tests use representative WireMock fixtures for movie `603` and TV series `1396`, without live TMDB
credentials. They validate the REST-to-Feign integration, not current live TMDB data.

## Prerequisites

- JDK 25.
- Docker with Docker Compose for local PostgreSQL.
- A free TMDb account and **API Key** from the
  [TMDb API settings](https://www.themoviedb.org/settings/api).
- An IDE such as IntelliJ IDEA (optional).

The application currently authenticates with TMDb's API Key as an `api_key` query parameter. It does not use the TMDb
API Read Access Token.

> Use Java 25 for the Gradle JVM and application runtime. The Gradle wrapper uses version 9.7.1, and the build pins a Java 25 toolchain.

## Getting started

Clone the repository and enter the project directory:

```bash
git clone https://github.com/anacmm/cinephilia-app.git
cd cinephilia-app
```

### Select Java 25

On macOS, select an installed JDK 25 (for example, Amazon Corretto or Eclipse Temurin):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
./gradlew --version
```

On other platforms, set `JAVA_HOME` to your JDK 25 installation and put its `bin` directory on `PATH`.
In IntelliJ IDEA, set both **Project SDK** and **Settings > Build, Execution, Deployment > Build Tools > Gradle >
Gradle JVM** to Java 25. Reload the Gradle project after changing them.

The backend uses Spring Boot 4.0.9, Spring Cloud 2025.1.4 and springdoc 3.0.3. Docker and CI also use Java 25.
This repository uses Gradle; there is no Maven `pom.xml`. Existing Java packages and artifact names remain unchanged.

## Local PostgreSQL

Start Docker Desktop (or your Docker daemon), then prepare an ignored local configuration file:

```bash
cp .env.example .env
chmod 600 .env
```

Set `POSTGRES_PASSWORD` in `.env` to a private local password, and set `DB_PASSWORD` to either that same
password or its Jasypt-encrypted `ENC(...)` equivalent. For encrypted values, use the same encryption key
and algorithm as the existing TMDB configuration, and supply `JASYPT_ENCRYPTOR_PASSWORD` separately.
The example intentionally contains no passwords. Compose refuses to start PostgreSQL until
`POSTGRES_PASSWORD` is set. Do not commit `.env` or production credentials; keep its permissions at `600`.
Compose also reads exported system environment variables, which override matching `.env` values.

```bash
docker compose up -d --wait postgres
docker compose ps
```

Compose runs PostgreSQL 17, binds it only to localhost, and stores data in the `postgres_data` named volume.
`docker compose stop postgres` stops it while preserving data. PostgreSQL's initialization variables only
apply when the volume is first created; editing `.env` does not change an existing database user's password.

| Variable | Local default | Purpose |
| --- | --- | --- |
| `DB_HOST` | `localhost` | Application database host |
| `DB_PORT` | `5432` | Application port and Compose's published PostgreSQL port |
| `DB_NAME` | `castquest` | Database name |
| `DB_USERNAME` | `castquest` | Database user |
| `DB_PASSWORD` | None | Application database password; plain text or Jasypt `ENC(...)` |
| `POSTGRES_PASSWORD` | None; required for Compose | Actual password used to initialize PostgreSQL |
| `DB_URL` | Built from host, port and name | Optional complete JDBC URL override for the application |
| `JASYPT_ENCRYPTOR_PASSWORD` | None | Decrypts the TMDB key and encrypted application database password |

Compose reads `.env` automatically. Spring Boot does not, so export its values in the terminal used for
Gradle or set them in the IDE's run configuration:

```bash
set -a
source .env
set +a
```

Use a shell-compatible `.env` file and quote values containing shell special characters. In deployed environments,
provide these values through your environment or secret manager. The Compose user is for local development;
deployment roles and database provisioning are outside this step. The application container would use the
database service hostname rather than `localhost` if run on a shared Docker network.

Quote encrypted passwords when sourcing `.env`, for example `DB_PASSWORD='ENC(...)'`. Docker cannot decrypt
Jasypt values; `POSTGRES_PASSWORD` must contain the actual password. Encrypting `DB_PASSWORD` does not protect
the separate plaintext Docker value in the same file. Keep the Jasypt encryption key outside `.env` and use an
exported environment variable or secret manager for it. Never bake passwords into the Dockerfile or image.

### Flyway and database verification

Spring Boot runs Flyway before initializing JPA. Hibernate uses `ddl-auto: validate`; it never creates or updates
tables. SQL initialization is disabled so Flyway is the schema authority. Spring Boot 4 uses the
[Flyway starter and PostgreSQL module](https://docs.spring.io/spring-boot/how-to/data-initialization.html).

Versioned SQL files live in `src/main/resources/db/migration`, named `V<number>__<description>.sql`.
`V1__initialize_database.sql` executes `SELECT 1` and establishes Flyway's schema history without domain tables.
`V2__create_players_and_game_sessions.sql` adds `players` and `game_sessions`. Add future changes as new migrations
(starting with `V3__...`); never edit migrations already applied to a shared database. Flyway validates checksums
on startup, automatic baselining is disabled, and Flyway clean is disabled.

After starting the application, verify connectivity and migration history:

```bash
curl --fail http://localhost:8080/actuator/health
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"'
```

Health should be `UP` (the database health indicator participates), and migration versions `1` and `2` should have
`success = t`. Startup logs also report Flyway migration/validation and JPA initialization.

### Players and game sessions

`Player` and `GameSession` live in the existing `model` package, with Spring Data JPA repositories in
`repository`. Both use generated UUIDs and database-generated `Instant` timestamps stored as PostgreSQL
`timestamp with time zone`. Guest player names are required, nonblank, and at most 50 characters; names are
not unique. There are no accounts or authentication.

A game session references an existing player without cascading deletes and starts as `IN_PROGRESS`.
`COMPLETED` and `ABANDONED` are the other persisted string statuses. Start and target TMDB person IDs must
be positive; completion fields are nullable, move counts and durations cannot be negative, and completion
cannot precede the start. A composite index on `(player_id, started_at DESC)` supports player-session access.
The nullable JSONB path maps to `List<Map<String, Object>>`, preserving ordered navigation node objects
without defining a REST path contract yet. The repositories expose standard CRUD only. No game endpoints,
state-transition rules, duration calculation, or leaderboard are implemented in this step.

## Configure the TMDb API key

The API key in `src/main/resources/application.yml` is stored as a Jasypt `ENC(...)` value. To replace it, choose an
encryption password and encrypt your new TMDb API Key with the same algorithm configured by the application.

First, build the project so that the Jasypt dependency is available locally:

```bash
./gradlew clean build
```

Then enter the API key and encryption password without saving them in your terminal history:

```bash
read -s "TMDB_API_KEY?TMDb API Key: "
echo
read -s "JASYPT_PASSWORD?New encryption password: "
echo

JASYPT_JAR=$(find "$HOME/.gradle/caches" -type f -name 'jasypt-1.9.3.jar' | head -1)

java -cp "$JASYPT_JAR" \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="$TMDB_API_KEY" \
  password="$JASYPT_PASSWORD" \
  algorithm=PBEWithMD5AndDES \
  ivGeneratorClassName=org.jasypt.iv.NoIvGenerator \
  verbose=false
```

Copy the command output into `src/main/resources/application.yml`:

```yaml
tmdb:
  base-url: https://api.themoviedb.org/3
  api-key: ENC(ENCRYPTED_OUTPUT)
```

Keep the encryption password private. Do not add it to `application.yml` or Git.

## Build and test

The regular build and existing WireMock/Mockito tests do not require PostgreSQL, Docker, live TMDB access,
or the Jasypt password. The `test` profile excludes database auto-configuration for these tests:

```bash
./gradlew clean build
```

With local PostgreSQL running and `.env` exported as above, run the separate database integration test:

```bash
./gradlew databaseTest
```

These tests re-enable database auto-configuration and start the full application on a random HTTP port with a test
TMDB key. They verify PostgreSQL connectivity, Flyway versions 1 and 2, Hibernate schema validation, UUID/timestamp
generation, duplicate guest names, session status updates, ordered JSONB path storage, and database constraints.
Repository test data is rolled back. Re-running migrations must apply no further changes. Use a dedicated test
database where possible. The tests fail if PostgreSQL is unavailable rather than silently skipping. CI runs them
against a disposable Compose database with a generated password, alongside the existing endpoint tests.
No extra test database dependencies are needed.

## Run the application

Start PostgreSQL and export `.env` as described above. Enter and export the same password that was used to
encrypt the TMDB API key, then start Spring Boot:

```bash
read -s "JASYPT_ENCRYPTOR_PASSWORD?Jasypt password: "
echo
export JASYPT_ENCRYPTOR_PASSWORD

./gradlew bootRun
```

The application is ready when the terminal shows:

```text
Started CinephiliaApplication
```

`bootRun` remains at an executing percentage while the web server is running. This is expected. Keep the terminal open
and use `Ctrl+C` to stop the application.

Useful local URLs:

- Application: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Health check: <http://localhost:8080/actuator/health>

Clear the secrets from the shell when finished:

```bash
unset TMDB_API_KEY JASYPT_PASSWORD JASYPT_ENCRYPTOR_PASSWORD DB_PASSWORD POSTGRES_PASSWORD
```

## Contributing

1. Fork the repository.
2. Create a branch with a descriptive name.
3. Implement and test your changes.
4. Open a pull request.

## License

This project is licensed under the MIT License. See `LICENSE` for details.

## Acknowledgments

- [The Movie Database (TMDb)](https://www.themoviedb.org/) for its movie and TV data API.
