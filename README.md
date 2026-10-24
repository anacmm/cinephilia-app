# CinephiliaApp

CinephiliaApp is a Java application for movie and TV enthusiasts. The aim of the game is to connect two actors through
their movie and TV credits in as few steps as possible.

This is an evolving study project created by [Ana Maestrello](https://github.com/anacmm).

## Features

- Browse popular and top-rated movies and TV shows.
- Browse people and their movie and TV credits.
- Connect actors through their shared work.
- Fetch current data from [The Movie Database (TMDb)](https://www.themoviedb.org/).
- Explore and test the API through Swagger UI.

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

The regular build and automated tests do not require the Jasypt password:

```bash
./gradlew clean build
```

## Run the application

Enter and export the same password that was used to encrypt the API key, then start Spring Boot:

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
unset TMDB_API_KEY JASYPT_PASSWORD JASYPT_ENCRYPTOR_PASSWORD
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
