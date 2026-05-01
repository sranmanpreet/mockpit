# syntax=docker/dockerfile:1.6
# Combined image: builds both the Angular client and the Spring Boot server in two parallel stages
# and packages them behind nginx. Useful for single-container quickstart deployments. For real
# production prefer the per-component images (server/Dockerfile + client/.../Dockerfile) so each
# can scale independently.

############################
# 1. Server build
############################
FROM maven:3.9.6-eclipse-temurin-11 AS server-build
WORKDIR /server
COPY server/pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline
COPY server/src ./src
RUN mvn -B -q -DskipTests package \
    && mv target/mockpit-*.jar target/mockpit-server.jar

############################
# 2. Client build
############################
FROM node:18.20.4-alpine AS client-build
WORKDIR /client
COPY client/mockpit-ui/package.json client/mockpit-ui/package-lock.json* ./
RUN npm ci
COPY client/mockpit-ui/. .
RUN npm run build -- --configuration=production --output-path=/client/dist

############################
# 3. Runtime
############################
FROM eclipse-temurin:11-jre-jammy

ENV LANG=C.UTF-8 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError"

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        nginx gettext-base wget ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system mockpit \
    && useradd --system --gid mockpit --home /app --shell /usr/sbin/nologin mockpit

WORKDIR /app

COPY --from=server-build /server/target/mockpit-server.jar /app/mockpit-server.jar
COPY --from=client-build /client/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
COPY entryPoint.sh /entryPoint.sh

RUN chmod +x /entryPoint.sh \
    && chown -R mockpit:mockpit /app /usr/share/nginx/html /var/log/nginx /var/lib/nginx \
    && mkdir -p /run/nginx \
    && chown -R mockpit:mockpit /run/nginx

USER mockpit

EXPOSE 80 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/entryPoint.sh"]
