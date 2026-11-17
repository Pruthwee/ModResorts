FROM maven:3.9.4-eclipse-temurin-8 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ src/
COPY WebContent/ WebContent/
RUN mvn clean package -DskipTests

FROM amazoncorretto:8
ENV TZ=UTC \
    JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    CATALINA_HOME=/opt/tomcat \
    CATALINA_BASE=/opt/tomcat
RUN groupadd -r tomcat && useradd -r -g tomcat -d /opt/tomcat -s /bin/false tomcat
WORKDIR /opt
RUN curl -L -o tomcat.tar.gz https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.99/bin/apache-tomcat-8.5.99.tar.gz \
    && tar -xzf tomcat.tar.gz \
    && mv apache-tomcat-8.5.99 /opt/tomcat \
    && rm -f tomcat.tar.gz \
    && rm -rf /opt/tomcat/webapps/examples /opt/tomcat/webapps/docs /opt/tomcat/webapps/host-manager /opt/tomcat/webapps/manager
COPY --from=builder /workspace/target/*.war /opt/tomcat/webapps/ROOT.war
RUN chown -R tomcat:tomcat /opt/tomcat
USER tomcat
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec /opt/tomcat/bin/catalina.sh run"]
