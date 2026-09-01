# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

# La primera etapa compila el JAR. Maven y el JDK no pasan a la imagen final.
FROM eclipse-temurin:21-jdk-noble@sha256:75ce56643243c3db632be2ef259625fb42ee3be1334389659f7a1a61acb78783 AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# La caché de Maven acelera compilaciones posteriores sin incorporarse a la imagen.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/*.jar /workspace/app.jar

# La segunda etapa contiene solamente Java, la aplicación y curl para el healthcheck.
FROM eclipse-temurin:21-jre-noble@sha256:96975602e131485862eb8cd32927face8a06d7591a5e865944b634a701d9df72 AS runtime

RUN groupadd --system --gid 10001 fincore \
    && useradd --system --uid 10001 --gid fincore --home-dir /app --shell /usr/sbin/nologin fincore

WORKDIR /app
COPY --from=build --chown=fincore:fincore /workspace/app.jar app.jar

# Un proceso sin privilegios reduce el impacto de una posible vulnerabilidad.
USER 10001:10001

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
