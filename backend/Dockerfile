# Multi-stage Docker build for CodeNova Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime container with compilers (Java 17, GCC/G++, Python 3, Node.js)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Install native execution tools: g++, gcc, python3, nodejs, npm
RUN apt-get update && apt-get install -y --no-install-recommends \
    g++ \
    gcc \
    python3 \
    nodejs \
    npm \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy packaged JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directories for runtime uploads and storage
RUN mkdir -p storage/host-verification-documents uploads/support

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
