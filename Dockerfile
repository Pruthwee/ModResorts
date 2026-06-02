# Multi-stage Dockerfile for ModResorts Java EE WAR application
# Builder stage uses Maven to produce the WAR artifact

FROM maven:3.9.4-eclipse-temurin-8 AS builder

WORKDIR /workspace

# Copy Maven build descriptor first for dependency caching
COPY pom.xml ./

# Pre-fetch dependencies (best-effort; will be revalidated during package)
RUN mvn -B -q dependency:go-offline || true

# Copy the full project source
COPY . .

# Build the WAR artifact
RUN mvn -B -q clean package -DskipTests

# Runtime stage - uses explicit base image as requested
FROM openjdk:8-jdk

ENV TZ=UTC \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=docker

# Create non-root user
RUN useradd -m -u 1001 appuser

WORKDIR /app

# Copy built WAR from builder stage
COPY --from=builder /workspace/target/modresorts-*.war /app/app.war

# Expose default servlet container port
EXPOSE 8080

USER appuser

# NOTE: This project is a traditional WAR and expects to run in an application server.
# In a real deployment you would typically deploy this WAR to a servlet container
# such as Tomcat, Jetty, or WebSphere Liberty. Here we assume an external container
# will consume the built WAR, so the container simply holds the artifact.

CMD ["/bin/sh", "-c", "echo 'This image contains modresorts WAR at /app/app.war. Deploy it to your Java EE application server.' && tail -f /dev/null"]
