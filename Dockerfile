# syntax=docker/dockerfile:1

# Build stage: use Maven to build the Spring Boot fat jar with dependencies
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
# Pre-fetch dependencies for better caching
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
# Build and repackage into a fat jar since we don't use the Spring Boot parent POM
RUN mvn -q -e -B -DskipTests package spring-boot:repackage

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/jqa-0.0.1-SNAPSHOT.jar app.jar
# Expose default Spring Boot port
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
