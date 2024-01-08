# CinephiliaApp

CinephiliaApp is a Java-based application designed for movie and TV show lovers. It's a fun and educational game where users must link two actors by exploring their work in movies and TV shows, aiming to do so in the fewest steps possible. This project is continually evolving and is authored by [Ana Maestrello](https://github.com/anacmm).

## Features

- Browse and search for movies, TV shows, and actors.
- Link actors by connecting their work in films and TV shows.
- Utilize The Movie Database (TMDb) API for rich movie and TV show data.
- Study project to improve your Java skills.

## Getting Started

### Prerequisites

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK) 11 or later installed.
- An IDE for Java development (e.g., IntelliJ IDEA, Eclipse).
- A free API key from [The Movie Database (TMDb)](https://www.themoviedb.org/documentation/api) for accessing movie and TV show data.

### Configuration

1. Clone the CinephiliaApp repository to your local machine:

   ```bash
   git clone https://github.com/anacmm/cinephilia-app.git

2. Open the project in your Java development IDE.

3. Create a application.yml file in the src/main/resources directory to store your TMDb API key. Add the API key like this:

   ```yaml
    tmdb:
    api-key: YOUR_TMDB_API_KEY
    Building and Running

4. To build and run the CinephiliaApp project, follow these steps:
 - Build the project using your IDE or Gradle:
 - If you're using Gradle, you can build the project by running the following command in the project directory:

    ```bash
    Copy code
    ./gradlew build

### Run the application:

Use your IDE to run the main application class, or run the JAR file generated in the build process.

Access the application in your web browser at http://localhost:8080 (default port). You can customize the port in the application.yml file if needed.

### Usage
Upon accessing the application, you can start playing the game.
Browse movies, TV shows, and actors to connect them in the fewest steps.
Explore different levels of difficulty and enjoy the game.

### Contributing
If you're interested in contributing to CinephiliaApp, please follow these steps:

- Fork the repository.
- Create a new branch with a descriptive name for your feature or fix.
- Implement your changes.
- Write tests to cover your code, if applicable.
- Make a pull request to the main repository.

### Issues and Support
If you encounter any issues or need support, please open an issue on the project's GitHub repository.

### License
This project is licensed under the MIT License. See the LICENSE file for more details.

### Acknowledgments
Ana C. M. Maestrello (@anacmm) for creating and maintaining the CinephiliaApp project.
The Movie Database (TMDb) for providing access to movie and TV show data via their API.
