# syntax=docker/dockerfile:1

FROM eclipse-temurin:24-jdk AS build
WORKDIR /workspace
COPY src ./src
RUN mkdir -p target/classes \
    && find src/main/java -name "*.java" -print0 | xargs -0 javac --release 21 -d target/classes \
    && jar --create --file target/jqa.jar --main-class md.fiodorov.jqa.JqaApplication -C target/classes .

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /workspace/target/jqa.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
