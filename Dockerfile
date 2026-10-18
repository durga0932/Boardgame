# Java 21 runtime image
FROM eclipse-temurin:21-jre

# Application working directory
WORKDIR /app

# Copy Spring Boot executable JAR
COPY target/database_service_project-0.0.5-SNAPSHOT.jar app.jar

# Application port
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

