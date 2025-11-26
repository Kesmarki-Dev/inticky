# Multi-stage build for Ticket Service
FROM gradle:8.4-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle/ gradle/

# Copy shared libraries
COPY shared/ shared/

# Copy the specific service
COPY services/ticket-service/ services/ticket-service/

# Build the application
RUN gradle :services:ticket-service:build -x test --no-daemon

FROM amazoncorretto:17-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR
COPY --from=builder /app/services/ticket-service/build/libs/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
