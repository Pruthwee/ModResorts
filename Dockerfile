# =============================================================================
# Stage 1: CSS Purge & Minification Build Stage
# =============================================================================
FROM node:18-alpine AS css-build

WORKDIR /css-build

COPY package.json ./
RUN npm install

COPY postcss.config.js ./

COPY WebContent/pikaday.css ./src/pikaday.css
COPY WebContent/styles.css  ./src/styles.css

COPY WebContent/index.html  ./src/index.html
COPY WebContent/login.jsp   ./src/login.jsp
COPY WebContent/main.js     ./src/main.js

RUN mkdir -p dist && \
    npx postcss src/styles.css  --config postcss.config.js -o dist/styles.css && \
    npx postcss src/pikaday.css --no-map -o dist/pikaday.css \
        --use cssnano

# =============================================================================
# Stage 2: HTML Minification Build Stage
# =============================================================================
FROM node:18-alpine AS html-build

WORKDIR /html-build

COPY package.json ./
RUN npm install

COPY WebContent/index.html ./src/index.html

RUN mkdir -p dist && \
    npx html-minifier-terser \
        --collapse-whitespace \
        --remove-comments \
        --remove-optional-tags \
        --remove-redundant-attributes \
        --remove-script-type-attributes \
        --remove-tag-whitespace \
        --use-short-doctype \
        --minify-css true \
        --minify-js true \
        src/index.html -o dist/index.html

# =============================================================================
# Stage 3: Maven Build Stage
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-11 AS maven-build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
COPY WebContent ./WebContent

COPY --from=css-build /css-build/dist/pikaday.css ./WebContent/pikaday.css
COPY --from=css-build /css-build/dist/styles.css  ./WebContent/styles.css
COPY --from=html-build /html-build/dist/index.html ./WebContent/index.html

RUN mvn clean package -DskipTests -B

# =============================================================================
# Stage 4: Production Runtime Image
# =============================================================================
FROM amazoncorretto:8

ENV TZ=UTC \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UnlockExperimentalVMOptions"

RUN groupadd -r appgroup && useradd -r -g appgroup -u 1001 appuser

WORKDIR /app

COPY --from=maven-build --chown=appuser:appgroup /workspace/target/modresorts-2.0.0.war /app/modresorts.war

EXPOSE 9080

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/modresorts.war"]
