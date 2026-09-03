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

## Prerequisites

- JDK 17.
- A free TMDb account and **API Key** from the
  [TMDb API settings](https://www.themoviedb.org/settings/api).
- An IDE such as IntelliJ IDEA (optional).

The application currently authenticates with TMDb's API Key as an `api_key` query parameter. It does not use the TMDb
API Read Access Token.

> The included Gradle 8.0.2 wrapper cannot run on Java 21. Use Java 17 for this project.

## Getting started

Clone the repository and enter the project directory:

```bash
git clone https://github.com/anacmm/cinephilia-app.git
cd cinephilia-app
```

### Select Java 17 with SDKMAN

If you use [SDKMAN](https://sdkman.io/), select the installed Temurin 17 SDK:

```bash
source "$SDKMAN_DIR/bin/sdkman-init.sh"
sdk use java 17.0.16-tem
java -version
```

In IntelliJ IDEA, also set **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM** to Java 17.

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

Keep the encryption password private. Do not add it to `application.yml`, `compose.yaml`, or Git.

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

## MongoDB

`compose.yaml` contains a development MongoDB service, but the current application does not have a MongoDB dependency
and does not require MongoDB to build or run.

## Contributing

1. Fork the repository.
2. Create a branch with a descriptive name.
3. Implement and test your changes.
4. Open a pull request.

## License

This project is licensed under the MIT License. See `LICENSE` for details.

## Acknowledgments

- [The Movie Database (TMDb)](https://www.themoviedb.org/) for its movie and TV data API.
