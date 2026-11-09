FROM maven:3.9.4-eclipse-temurin-8 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY src ./src/
COPY WebContent ./WebContent/

RUN mvn clean package -DskipTests

FROM amazoncorretto:8

RUN groupadd -r modresorts && useradd -r -g modresorts modresorts

WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV TZ=UTC
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

USER modresorts

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 -jar app.jar"]
