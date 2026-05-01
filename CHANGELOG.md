# Changelog

All notable changes to this project will be documented in this file. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-05-01

Toolchain refresh: project now compiles, runs, and tests on the latest LTS-grade stack. No
behavioural changes; this is purely a platform upgrade.

### Changed
- **JDK runtime upgraded to Eclipse Temurin 26** (`eclipse-temurin:26-jre-noble`); source/target
  bytecode bumped from Java 11 to **Java 21** so artefacts remain consumable on JDK 21+.
- **Spring Boot 2.7.18 → 3.5.14** (full Jakarta EE 9+ migration: `javax.*` → `jakarta.*` across
  Servlet, Persistence, Validation, Annotation, Transaction).
- **Spring Security 6** API surface adopted (`securityMatcher`, `requestMatchers`, explicit
  `CsrfTokenRequestAttributeHandler` configuration for the Angular SPA).
- **JJWT 0.11 → 0.12.7**, **Bucket4j 7 → 8.14.0**, **springdoc-openapi 1 → 2.8.14** (web-mvc-ui
  starter), **GraalVM 22 → 24.1.2** (Community), **JaCoCo 0.8.13 → 0.8.14** (Java 26 class-file
  support), **Liquibase 4.27 → 4.32.0**, **PostgreSQL JDBC 42.7.4 → 42.7.7**, **MapStruct 1.5.5
  → 1.6.3**, **Lombok 1.18.34 → 1.18.42**, **Caffeine 3.1 → 3.2.2**, **Nimbus JOSE+JWT 9 → 10.4**,
  **WireMock 3.6 → 3.13.2**, **OWASP Dependency-Check 9 → 12.1.5**.
- **Angular 15.2 → 21.2.11** (`@angular-devkit/build-angular:browser` → `@angular/build:application`
  esbuild builder, Material M3 prebuilt theme, `provideHttpClient` + `withXsrfConfiguration` +
  `withInterceptorsFromDi` instead of the deprecated `HttpClientModule` / `HttpClientXsrfModule`,
  every `@Component` / `@Pipe` declared with `standalone: false` to keep the existing NgModule
  layout working). **TypeScript 4.9 → 5.9.3**, **zone.js 0.12 → 0.15**, **ngx-toastr 16 → 20.0.5**.
- **Node 18 → 22** (Docker images: `node:22-alpine`).
- **PostgreSQL 15 → 18** (`postgres:18-alpine` for both runtime and Testcontainers).
- **GitHub Actions** server / client / security-scan jobs bumped to JDK 26 + Node 22.
- `tsconfig.json` switched to `moduleResolution: "bundler"` (required by Angular 17+).

### Fixed
- `JwtAuthValidatorTest.rejectsTamperedSignature` now flips a mid-signature character — JJWT
  0.12's lenient base64url decoder treated `'A'` and `'B'` as the same trailing byte for
  HMAC-SHA256, masking the tampering.
- `JavaScriptExecutionService` now detects when GraalVM Community refuses the
  `MaxStatements` sandbox limit (an enterprise-only feature) and gracefully falls back to the
  wall-clock timeout watchdog instead of failing on every JS evaluation.
- `SafeHttpClient` gains a test-only `mockpit.http-client.allow-loopback` escape hatch so the
  WireMock-based OAuth2 introspection tests can reach `127.0.0.1` without disabling the SSRF
  filter in production.
- `AuthConfig` Jackson deserialisation tolerates the `type` discriminator being present in the
  payload (kept visible for the UI) without requiring a setter on the abstract class.
- `BasicAuthConfig.passwordHash` is now persisted/round-tripped correctly; the
  `AuthConfigCodec#redactForResponse` step strips it before it reaches the UI.
- `MockpitApplicationTests` renamed to `MockpitApplicationIT` so it is excluded from the
  `mvn test` (unit) phase and only runs in `mvn verify` where Docker is expected.

### Migration notes
- Operators must run on **JDK 21 or newer** (JDK 26 recommended). The runtime container ships
  with JDK 26 and `--enable-native-access=ALL-UNNAMED` baked into `JAVA_OPTS`.
- The Angular dev-server output path changed shape (`dist/` instead of `dist/mockpit-ui/`); the
  Dockerfiles and reverse-proxy configs already reflect this.
- Tailwind, ngx-toastr CSS, and Material theme imports continue to work unchanged for end-users.

## [2.0.0] - 2026-04-29

This is a breaking, foundational release. Multi-tenancy, per-mock authentication, and a top-to-
bottom security pass make Mockpit suitable for hosting on the open internet.

### Added
- **User accounts**: signup, login, logout, password-reset stubs, BCrypt-hashed credentials,
  account lockout after 5 failed attempts, optional email verification.
- **JWT-in-HttpOnly-cookie sessions** for the admin SPA, with CSRF tokens on state-changing
  endpoints.
- **Per-mock authentication**: Basic, JWT (HS / RS / ES), OAuth2 Resource Server (OIDC discovery
  + JWKS), OAuth2 Introspection (RFC 7662). User-configurable failure response with a sensible
  RFC-compliant default.
- **AES-256-GCM at-rest encryption** for per-mock secrets (HMAC keys, OAuth2 client secrets,
  PEM material). Operator-provided key via `MOCKPIT_SECRET_CIPHER_KEY`.
- **SSRF-hardened outbound HTTP client** for JWKS / OIDC discovery / introspection calls:
  scheme allowlist, RFC1918 / loopback / metadata-IP blocking, optional host allowlist, redirect-
  following disabled, response-size cap.
- **Sandboxed GraalJS execution**: shared engine, `HostAccess.NONE`, no IO / threads / native,
  statement count limit, wall-clock timeout watchdog, output byte cap.
- **Per-IP token-bucket rate limiting** (Bucket4j) on both admin and live surfaces.
- **Strict CORS allowlist** + comprehensive **security headers** (HSTS, CSP, XFO, XCTO,
  Referrer-Policy, Permissions-Policy) on every response.
- **Reserved-prefix denylist** so user mocks cannot shadow `/native`, `/auth`, `/actuator`,
  `/swagger-ui`, `/v3/api-docs`, or `/error`.
- **Spring Boot Actuator** with `/health` public, `/prometheus` admin-protected.
- **Structured JSON logging** via Logback + Logstash encoder, with `X-Request-Id` MDC correlation.
- **Multi-stage Dockerfiles** (server + client + combined) running as non-root with healthchecks.
- **`docker-compose.prod.yml`** with resource limits, restart policies, env-only secrets, and a
  TLS-ready nginx reverse-proxy config (`nginx.prod.conf`).
- **Test suite**: JUnit 5 + Mockito + AssertJ + Spring Boot Test + Testcontainers (Postgres) +
  WireMock + Spring Security Test + JaCoCo with coverage gate. Frontend: Karma + Jasmine specs
  for AuthService, AuthGuard, AuthConfigComponent.
- **`SECURITY.md`**, **`CONTRIBUTING.md`**, **`CODE_OF_CONDUCT.md`**, **`.env.example`**.
- **Per-user mock quota** to prevent a single tenant from exhausting database capacity.
- **POST `/native/api/mocks/{id}/auth/test`** endpoint for the front-end "Test auth" button.

### Changed
- **Spring Boot 2.6.4 → 2.7.18**, **Liquibase 4.20 → 4.27**, **PostgreSQL JDBC 42.6.0 → 42.7.4**,
  **Lombok 1.18.22 → 1.18.34**, **MapStruct 1.4.2 → 1.5.5**, **springdoc-openapi-ui 1.6.6 →
  1.8.0**, **GraalVM JS 22.3.2 → 22.3.5**, etc.
- **Datasource** now flows through Spring's HikariCP autoconfiguration instead of the
  legacy `DriverManagerDataSource`.
- **Configuration**: all secrets must come from environment variables. `application-prod.yml`
  fails-fast at boot if `MOCKPIT_JWT_SECRET`, `MOCKPIT_SECRET_CIPHER_KEY` or
  `MOCKPIT_ALLOWED_ORIGINS` are missing.
- **Pageable** capped at 200 items per page; mock import capped at 1,000 mocks per request;
  search query capped at 100 characters; multipart capped at 2 MB by default.
- **`MockService.deleteAllMocks`** now scoped to the calling user (admins still wipe everything).
- **Mock entity** gains `userId`, `authType`, `authConfigJson`, `authFailureStatus`,
  `authFailureBody`, `authFailureContentType` (Liquibase changeset
  `release_2.0/add_users_and_ownership.xml`).

### Fixed
- Duplicate `setRoute(...)` call in `MockService.updateMock` that overwrote slash-normalised
  paths with the un-normalised version.
- `RestExceptionHandler` no longer leaks raw exception messages or stack traces to API clients
  outside the `dev` profile.
- `LiveResource` URI is sanitised for log output to defend against CRLF log injection (CWE-117).
- Tracked `derby.log` removed; `.gitignore` tightened to keep runtime artefacts out of git.

### Removed
- `MiscResource` catch-all (replaced by `ReservedPathFilter`).
- Insecure JMX exposure (`-Dcom.sun.management.jmxremote.port=9010`) from `entryPoint.sh` and
  `start.bat`.
- Hibernate Envers `@Audited` annotation (the audit tables it required were never shipped).
- Hard-coded `mockpitadmin` / `admin` Postgres credentials in compose files.
- Wildcard CORS configuration (`allowed-origins: "*"`).
- `build-docker-compose.yml` (folded into `docker-compose.prod.yml`).

### Migration notes
- Set `SPRING_PROFILES_ACTIVE=prod` in production. The prod profile fails-fast on missing
  secrets — see `.env.example` for the full list.
- Pre-2.0 mocks have `user_id IS NULL`. They are visible to admins only; reassign via the API
  or a SQL migration once you have created the corresponding user accounts.
- The pre-2.0 image exposed JMX on port 9010; remove that port mapping from your deployment
  scripts.
