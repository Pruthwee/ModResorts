# Stage 1: Source stage — holds source so PurgeCSS can scan templates
FROM maven:3.9.4-eclipse-temurin-8 AS sources
WORKDIR /workspace
COPY src/ src/

# Stage 2: PurgeCSS stage — strips unused CSS rules for minimal production image
FROM node:18-alpine AS purger
WORKDIR /purge
RUN npm install --save-dev @fullhuman/postcss-purgecss purgecss
COPY --from=sources /workspace/src/main/resources/static/styles.css /purge/static/styles.css
COPY --from=sources /workspace/src/main/resources/static/index.html /purge/static/index.html
COPY --from=sources /workspace/src/main/resources/static/login.html /purge/static/login.html
RUN npx purgecss \
      --css /purge/static/styles.css \
      --content /purge/static/index.html /purge/static/login.html \
      --output /purge/out

# Stage 3: Build stage — compiles the JAR with purged CSS included
FROM maven:3.9.4-eclipse-temurin-8 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ src/
# Overwrite source CSS with purged CSS before packaging
COPY --from=purger /purge/out/styles.css /workspace/src/main/resources/static/styles.css
RUN mvn clean package -DskipTests -B

# Stage 4: Runtime stage
FROM eclipse-temurin:8-jre
WORKDIR /app
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV TZ=UTC
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV SPRING_PROFILES_ACTIVE=prod
# Health check is handled by the application's own /actuator/health endpoint
# The platform probes this endpoint; no HEALTHCHECK instruction here
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
