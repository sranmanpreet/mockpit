@echo off
REM Mockpit local quickstart. Override defaults via environment variables before invoking.
REM Required in production: MOCKPIT_JWT_SECRET, MOCKPIT_SECRET_CIPHER_KEY, MOCKPIT_ALLOWED_ORIGINS.
REM Default image is built from the Spring Boot 3.5 / Angular 21 / JDK 26 stack.

set "IMAGE=sranmanpreet/mockpit:2.1.0-RELEASE"
if not "%1"=="" set "IMAGE=%1"

docker run --rm --name mockpit ^
    -e SPRING_PROFILES_ACTIVE=dev ^
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/mockpitdb ^
    -e SPRING_DATASOURCE_USERNAME=postgres ^
    -e SPRING_DATASOURCE_PASSWORD=postgres ^
    -e MOCKPIT_ALLOWED_ORIGINS=http://localhost:3000 ^
    -e backendUrl=http://localhost:8080 ^
    -p 3000:80 -p 8080:8080 ^
    %IMAGE%
