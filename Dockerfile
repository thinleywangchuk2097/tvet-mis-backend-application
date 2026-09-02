# Stage 1: Build the project
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the project with Alpine (No Go vulnerabilities!)
FROM eclipse-temurin:21-jre-alpine

# ✅ FIX: Update all Alpine packages to latest versions
RUN apk update && apk upgrade --no-cache

# Set working directory
WORKDIR /app

# Create upload directories (will be overridden by PVC)
# Alpine uses 'mkdir -p' just like Ubuntu
RUN mkdir -p /app/uploads/profile-images /app/uploads/documents

# Create logs directory
RUN mkdir -p /app/logs

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]