# Multi-stage Dockerfile: Builds with Maven and runs on lightweight OpenJDK 21 JRE
FROM docker.io/library/maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Create unprivileged system user and group adhering to CIS container benchmarks
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built jar from build stage with strict ownership
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Enforce least-privilege non-root execution
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
