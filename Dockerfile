# syntax=docker/dockerfile:1

# Build stage: use Maven to build the Spring Boot fat jar with dependencies
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
# Pre-fetch dependencies for better caching
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/jqa-0.0.1-SNAPSHOT.jar app.jar
# Expose default Spring Boot port
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
