# Multi-stage build for Notification Service
FROM gradle:8.4-jdk17 AS builder

WORKDIR /app
COPY . .

# Build the application
RUN gradle :services:notification-service:build -x test --no-daemon

FROM openjdk:17-jre-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=builder /app/services/notification-service/build/libs/*.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
