# syntax=docker/dockerfile:1.6
# Multi-stage build for the Mockpit server. The runtime image is distroless-ish - a small
# Eclipse Temurin JRE image running as a non-root user with a healthcheck against /actuator/health.

############################
# 1. Maven build
############################
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && mv target/mockpit-*.jar target/app.jar

############################
# 2. Runtime
############################
FROM eclipse-temurin:11-jre-jammy

ENV LANG=C.UTF-8 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

RUN groupadd --system mockpit \
    && useradd --system --gid mockpit --home /app --shell /usr/sbin/nologin mockpit \
    && mkdir -p /app \
    && chown -R mockpit:mockpit /app

WORKDIR /app
COPY --from=build --chown=mockpit:mockpit /workspace/target/app.jar /app/mockpit-server.jar

USER mockpit

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/mockpit-server.jar"]
