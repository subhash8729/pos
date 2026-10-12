# ==========================================
# Stage 1: Build the Spring Boot application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install bash for the maven wrapper script
RUN apk add --no-cache bash

# Copy Maven wrapper configuration and descriptor
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure Unix line endings and executable permissions for mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Pre-fetch dependencies for efficient Docker layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy application source code and compile production JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Stage 2: Minimal Production Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a dedicated non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the generated JAR from builder stage
COPY --from=builder /app/target/app.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Default port 5000; cloud platforms inject $PORT dynamically
EXPOSE 5000

# JVM options optimized for container memory constraints
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
