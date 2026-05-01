# Mockpit

[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.5.14-6db33f)
![Java](https://img.shields.io/badge/java-21%20%2F%2026-007396)
![Angular](https://img.shields.io/badge/angular-21-dd0031)
![Node](https://img.shields.io/badge/node-22-339933)
![Postgres](https://img.shields.io/badge/postgres-18-336791)

Mockpit is a self-hosted, multi-tenant mock REST API server. Define endpoints in a friendly UI,
return static or fully dynamic JavaScript-evaluated responses, and protect them with the
authentication scheme of your choice — all without touching a line of code.

> 2.0 release introduces user accounts, per-mock authentication (Basic / JWT / OAuth2 RS /
> OAuth2 introspection), an SSRF-hardened HTTP client, AES-GCM at-rest encryption for secrets,
> rate-limiting, and a production-shaped Docker / Compose layout. See [`CHANGELOG.md`](CHANGELOG.md)
> for the full list of changes and the [security advisory list](SECURITY.md) for what was fixed.

---

## Table of contents

1. [Why Mockpit?](#why-mockpit)
2. [Feature matrix](#feature-matrix)
3. [Quickstart](#quickstart)
4. [Per-mock authentication](#per-mock-authentication)
5. [Production deployment](#production-deployment)
6. [Configuration reference](#configuration-reference)
7. [Development](#development)
8. [Security](#security)
9. [Contributing](#contributing)

---

## Why Mockpit?

Sometimes you need an HTTP endpoint that returns *exactly* the response you want, right now,
without spinning up a backend or convincing your colleagues to merge a stub. Mockpit is the
quickest way to:

- Unblock front-end work while the backend is still in flight.
- Reproduce flaky third-party APIs in CI.
- Demo a feature without depending on a live integration.
- Stress-test client-side error handling with crafted 4xx / 5xx responses.
- Provide protected internal endpoints to teams without writing one-off services.

## Feature matrix

| Capability                                            | 1.x | **2.0** |
| ----------------------------------------------------- | :-: | :----:  |
| CRUD UI for REST mocks                                |  ✓  |   ✓    |
| Static + JavaScript-evaluated response bodies         |  ✓  |   ✓    |
| Path / query parameter binding                        |  ✓  |   ✓    |
| Mock import / export                                  |  ✓  |   ✓    |
| Multi-tenant user accounts                            |  ✗  |   ✓    |
| Per-mock authentication (Basic / JWT / OAuth2)        |  ✗  |   ✓    |
| AES-GCM encryption-at-rest for secrets                |  ✗  |   ✓    |
| OWASP Top-10 baseline                                 |  ✗  |   ✓    |
| Spring Security + CSRF + HSTS + CSP                   |  ✗  |   ✓    |
| Sandboxed GraalJS execution (no IO/threads)           |  ✗  |   ✓    |
| Per-IP rate limiting                                  |  ✗  |   ✓    |
| Prometheus metrics + structured JSON logs             |  ✗  |   ✓    |
| Production-ready multi-stage Docker images            |  ✗  |   ✓    |
| TLS-ready reverse-proxy config                        |  ✗  |   ✓    |

## Quickstart

The fastest path is the dev compose file:

```bash
git clone https://github.com/sranmanpreet/mockpit.git
cd mockpit
docker compose up
```

That brings up:

- `mockpit-server` (Spring Boot) on `http://localhost:8080`
- `mockpit-client` (Angular SPA) on `http://localhost:4200`
- `mockpit-db` (Postgres 18) on `localhost:5432`

A bootstrap admin account is seeded on first run with the credentials in `docker-compose.yml`
(`admin@mockpit.local` / `ChangeMeNow123!`). Sign in, change the password immediately, and you
are ready to create mocks.

> Stop using the bootstrap admin in production. Set `MOCKPIT_BOOTSTRAP_ADMIN_ENABLED=false`
> after creating your first user.

## Per-mock authentication

Every mock has an **Authentication** section in the form. Choose a scheme from the dropdown and
supply the parameters; the form switches to the right inputs per scheme.

### None
Default. The mock is public; anyone who can reach the URL gets the response.

### Basic
HTTP Basic auth. Configure a username and password — the password is hashed with BCrypt before
being stored, never in plaintext. Failed attempts return `401 Unauthorized` with a
`WWW-Authenticate: Basic realm="..."` challenge.

```bash
curl -u alice:CorrectHorseBatteryStaple https://mockpit.example.com/my/protected/mock
```

### JWT (Bearer)
Verify a JWT signed with HS256/384/512, RS256/384/512 or ES256/384/512. Provide either a shared
HMAC secret or a JWKS URI / PEM-encoded public key. Optional checks: required issuer, audience
list, scope list, custom `claim=value` pairs, configurable clock skew.

```bash
curl -H "Authorization: Bearer eyJhbGciOi..." https://mockpit.example.com/my/protected/mock
```

### OAuth2 Resource Server
Validate access tokens issued by an OIDC provider. Supply the issuer URL — Mockpit fetches the
discovery document, caches the JWKS, and validates the token signature plus the standard
`iss`/`aud`/`exp` claims. Audience and scope checks are configurable.

### OAuth2 Introspection (RFC 7662)
For opaque tokens. Mockpit POSTs the token to your introspection endpoint with its client
credentials and accepts the response when `active=true`. Positive responses are cached for the
configured TTL to avoid hammering the AS on every request.

### Custom failure response
For any scheme you can override what Mockpit returns when authentication fails — status code,
content-type, and response body. If left blank, Mockpit returns `401 Unauthorized` with a
JSON error envelope.

### Test auth from the UI
The form has a **Test auth** button: paste a sample header (e.g. `Authorization: Bearer ...`)
and Mockpit will run the configured validator against your mock and tell you whether it would
have accepted the request — without actually serving the mock body.

## Production deployment

```bash
cp .env.example .env
# Edit .env to fill in real secrets and origins.

docker compose -f docker-compose.prod.yml up -d --build
```

`docker-compose.prod.yml` builds both images locally, applies CPU / memory limits, plumbs
secrets via env vars (no defaults — the stack will refuse to start if anything is missing) and
fronts the SPA + API with nginx. Drop a TLS-enabled `nginx.prod.conf` and Let's Encrypt
certificates into the right paths and you have an internet-hostable Mockpit.

[![Deploy to Fly.io](https://fly.io/static/images/launch/deploy.svg)](https://fly.io/launch?repo=https%3A%2F%2Fgithub.com%2Fsranmanpreet%2Fmockpit)
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https%3A%2F%2Fgithub.com%2Fsranmanpreet%2Fmockpit)
[![Deploy to Railway](https://railway.app/button.svg)](https://railway.app/new/template?template=https%3A%2F%2Fgithub.com%2Fsranmanpreet%2Fmockpit)

## Configuration reference

All `mockpit.*` configuration keys can be overridden by environment variables. The most important
ones (and their `application.yml` keys):

| Env var                              | Property                                  | Required (prod) | Notes                                                       |
| ------------------------------------ | ----------------------------------------- | :-------------: | ----------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`              | `spring.datasource.url`                   |       ✓         | JDBC URL.                                                   |
| `SPRING_DATASOURCE_USERNAME`         | `spring.datasource.username`              |       ✓         |                                                             |
| `SPRING_DATASOURCE_PASSWORD`         | `spring.datasource.password`              |       ✓         |                                                             |
| `MOCKPIT_JWT_SECRET`                 | `mockpit.security.jwt.secret`             |       ✓         | ≥ 32 bytes of entropy (base64).                             |
| `MOCKPIT_SECRET_CIPHER_KEY`          | `mockpit.security.secret-cipher-key`      |       ✓         | Exactly 32 bytes (base64). Used for AES-GCM encrypt-at-rest. |
| `MOCKPIT_ALLOWED_ORIGINS`            | `mockpit.cors.allowed-origins`            |       ✓         | Comma-separated. No wildcards.                              |
| `MOCKPIT_ALLOWED_HOSTS`              | `mockpit.http-client.allowed-hosts`       |                 | Optional allowlist for outbound JWKS / introspection calls. |
| `MOCKPIT_RATELIMIT_ENABLED`          | `mockpit.ratelimit.enabled`               |                 | Default `true`. Set `false` to disable per-IP throttling.   |
| `MOCKPIT_RATELIMIT_ADMIN`            | `mockpit.ratelimit.admin-requests-per-minute` | | Default 300.                                            |
| `MOCKPIT_RATELIMIT_LIVE`             | `mockpit.ratelimit.live-requests-per-minute`  | | Default 600.                                            |
| `MOCKPIT_JS_TIMEOUT_MS`              | `mockpit.js-sandbox.timeout-ms`           |                 | Wall-clock cap for user JS. Default 1000ms.                 |
| `MOCKPIT_JS_MAX_STATEMENTS`          | `mockpit.js-sandbox.max-statements`       |                 | GraalJS statement-count limit. Default 100,000.             |
| `MOCKPIT_JS_MAX_OUTPUT`              | `mockpit.js-sandbox.max-output-bytes`     |                 | Max bytes returned from a JS-evaluated body. Default 256KB. |
| `MOCKPIT_BOOTSTRAP_ADMIN_*`          | `mockpit.security.bootstrap-admin.*`      |                 | Optional first-run admin seed.                              |

## Development

Backend (Spring Boot 3.5.14 / Java 21 source-target, runs on JDK 26):

```bash
cd server
./mvnw spring-boot:run

# Tests
./mvnw verify          # runs unit + integration tests with JaCoCo
./mvnw -Psecurity-scan verify   # adds OWASP dependency-check
```

Frontend (Angular 21 / Node 22):

```bash
cd client/mockpit-ui
npm install
npm start              # http://localhost:4200
npm run test:ci        # Karma + Jasmine, headless Chrome, with coverage
```

The integration test suite spins up a real Postgres via Testcontainers (Docker required) and a
WireMock instance for OIDC / introspection scenarios.

## Security

Found something suspicious? Please follow the responsible-disclosure guidelines in
[`SECURITY.md`](SECURITY.md) before opening a public issue.

Mockpit 2.0 ships with the following baked in:

- BCrypt-hashed admin passwords; per-mock Basic auth passwords also BCrypt-hashed.
- AES-256-GCM encryption-at-rest for per-mock secrets (HMAC keys, OAuth2 client secrets, PEMs).
- JWT-in-HttpOnly-cookie sessions with CSRF tokens for state-changing admin endpoints.
- SSRF defence on every outbound auth call: scheme allowlist, RFC1918/loopback/metadata blocking,
  optional host allowlist, redirect-following disabled.
- Sandboxed GraalJS: `HostAccess.NONE`, no IO / threads / native, statement count limit, wall-clock
  watchdog, output cap.
- Strict CORS allowlist (no wildcards in production).
- HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy and Permissions-Policy on
  every response.
- Per-IP token-bucket rate limiting on both the admin and live surfaces.
- Stack traces never leak to API clients (only logged server-side).
- All admin endpoints require authentication; ownership is enforced on every mock fetch (no IDOR).
- Reserved-prefix denylist so user-created mocks cannot shadow `/native`, `/auth`, `/actuator`, etc.

## Contributing

PRs are welcome — please read [`CONTRIBUTING.md`](CONTRIBUTING.md) for the workflow and
[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) for community guidelines.
