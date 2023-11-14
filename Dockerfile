# Use the official OpenJDK base image
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/cinephilia-0.0.1-SNAPSHOT.jar /app/cinephilia-0.0.1-SNAPSHOT.jar

# Expose the port your application runs on (default is 8080)
EXPOSE 8080

# Define the command to run your application
CMD ["java", "-jar", "cinephilia-0.0.1-SNAPSHOT.jar"]
