# Multi-stage Docker build for Spring Boot Backend on Render
# Stage 1: Build JAR using Maven with Google Mirror (bypasses Maven Central 429 rate limit)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Ensure settings.xml is installed in standard Maven locations
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/settings.xml
COPY settings.xml /usr/share/maven/conf/settings.xml
COPY settings.xml .
COPY pom.xml .
COPY src ./src

# Build JAR using settings.xml with Google mirror configuration
RUN mvn clean package -DskipTests -s /root/.m2/settings.xml

# Stage 2: Minimal Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/backend-1.0.0.jar app.jar

# Default to production profile inside Docker
ENV SPRING_PROFILES_ACTIVE=prod

# Render assigns dynamic PORT environment variable (Render standard port: 10000, local: 8080)
EXPOSE 10000 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
