# ==============================================================================
# Multi-Stage Dockerfile for Task & Project Management System (Java 21)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Stage
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copy Maven descriptor and download dependencies to optimize Docker layer cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src ./src

# Package production application artifact (skipping tests during image assembly)
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Production Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-privileged system user and group for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built JAR from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Ensure appropriate file ownership
RUN chown -R appuser:appgroup /app

# Switch to unprivileged execution user
USER appuser

EXPOSE 8080

# Production-tuned JVM container options
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]
