# Build stage
FROM maven:3.9.4-eclipse-temurin-8 AS builder
WORKDIR /workspace

# Copy pom.xml first for dependency resolution
COPY pom.xml .

# Download Tomcat (for the runtime stage)
RUN curl -sL https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.92/bin/apache-tomcat-8.5.92.tar.gz -o /tmp/tomcat.tar.gz \
    && tar -xzf /tmp/tomcat.tar.gz -C /tmp/ \
    && rm -f /tmp/tomcat.tar.gz

# Copy source and build
COPY src/main/java src/main/java
COPY src/main/resources src/main/resources
COPY WebContent WebContent

# Build the WAR
RUN mvn clean package -DskipTests

# Deploy the WAR to Tomcat
RUN cp target/*.war /tmp/apache-tomcat-8.5.92/webapps/ROOT.war

# Runtime stage
FROM eclipse-temurin:8-jre
LABEL maintainer="ModResorts Team"

# Copy Tomcat from build stage
COPY --from=builder /tmp/apache-tomcat-8.5.92 /opt/tomcat

# Set environment variables
ENV CATALINA_HOME=/opt/tomcat \
    CATALINA_BASE=/opt/tomcat \
    JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    TZ=UTC \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Expose port 8080
EXPOSE 8080

# Create non-root user
RUN useradd -r -d /opt/tomcat -s /sbin/nologin tomcat && chown -R tomcat:tomcat /opt/tomcat

USER tomcat

# Start Tomcat
ENTRYPOINT ["/opt/tomcat/bin/catalina.sh", "run"]
