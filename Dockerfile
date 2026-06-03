# Dockerfile for ModResorts WAR application built with Maven
# Multi-stage build: Maven builder + OpenJDK 8 runtime (explicit base image)

FROM maven:3.8.6-openjdk-8-slim AS BUILDER

WORKDIR /workspace

# Copy Maven build descriptor first for dependency caching
COPY pom.xml ./

# Pre-download dependencies (best-effort; will be revalidated on full build)
RUN mvn -B -q dependency:go-offline || true

# Copy the full project (excluding wrapper files via .dockerignore)
COPY . .

# Build WAR artifact, skipping tests for faster container builds
RUN mvn -B clean package -DskipTests

# Runtime image uses explicit base image provided by user
FROM openjdk:8-jdk

ENV TZ=UTC \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions -XX:MaxRAMPercentage=75.0" \
    APP_NAME="modresorts" \
    APP_VERSION="2.0.0"

# Create non-root user
RUN useradd -m -u 1001 appuser

WORKDIR /app

# Copy built WAR from builder stage
COPY --from=BUILDER /workspace/target/*.war /app/app.war

# Expose the application port used by the servlet container
EXPOSE 9080

USER appuser

# Default command runs the WAR using an embedded servlet container (e.g., Jetty/Tomcat)
# Adjust this if you deploy the WAR into an external container instead.
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.war"]
