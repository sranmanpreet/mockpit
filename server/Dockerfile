# syntax=docker/dockerfile:1.7
# Multi-stage build for the Mockpit server. Build runs on Maven 3.9 / Eclipse Temurin 21 (the
# bytecode target), runtime image is the JDK 26 Temurin image to match what the user has
# installed locally and to take advantage of the latest GC + JIT improvements.

############################
# 1. Maven build
############################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && mv target/mockpit-*.jar target/app.jar

############################
# 2. Runtime
############################
FROM eclipse-temurin:26-jre-noble

ENV LANG=C.UTF-8 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom --enable-native-access=ALL-UNNAMED"

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
