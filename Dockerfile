# Multi-stage Docker build for Spring Boot Backend on Render
# Stage 1: Build JAR using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/backend-1.0.0.jar app.jar

# Render assigns dynamic PORT environment variable (defaults to 8080 locally)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
