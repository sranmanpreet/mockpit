# Contributing to Mockpit

Thanks for your interest in improving Mockpit. This document covers the workflow, the local
development setup, and the standards we hold PRs to.

## Code of conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). By participating you agree
to abide by its terms.

## Quick start (local development)

```bash
# Backend
cd server
./mvnw spring-boot:run

# Frontend
cd client/mockpit-ui
npm install
npm start
```

## Tests

Every PR must include tests for new behaviour and pass the existing suite:

```bash
cd server
./mvnw verify             # unit + integration tests, JaCoCo coverage gate

cd client/mockpit-ui
npm run test:ci           # Karma + Jasmine, headless Chrome, with coverage
```

The integration tests require Docker (Testcontainers spins up a Postgres). If you cannot run
them locally, ask a maintainer to run them via CI on your behalf.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) where possible:

- `feat: ...` — user-visible feature
- `fix: ...` — bug fix
- `chore: ...` — non-functional / tooling change
- `docs: ...` — documentation only
- `test: ...` — tests only
- `refactor: ...` — refactoring with no behavioural change
- `perf: ...` — performance improvement
- `build: ...` — build / dependency change
- `ci: ...` — CI configuration change
- `security: ...` — security fix or hardening

Breaking changes get a `!` after the type and a `BREAKING CHANGE:` footer.

## Pull request checklist

- [ ] Tests added or updated for the change.
- [ ] Documentation updated (README / CHANGELOG / inline comments where appropriate).
- [ ] No secrets or credentials in committed files.
- [ ] No new transitive dependencies with known critical/high CVEs (`mvn -Psecurity-scan verify`
      and `npm audit --production`).
- [ ] Coverage gate still passes (`./mvnw verify`).
- [ ] No new lints introduced.

## Reporting security issues

Please follow [`SECURITY.md`](SECURITY.md) — do **not** open a public issue for a vulnerability.

## Project structure

```
client/                 # Angular 21 SPA (esbuild builder, Karma/Jasmine)
server/                 # Spring Boot 3.5.14 backend (Java 21 bytecode, JDK 26 runtime)
db/                     # Database initialisation scripts
documentation/          # User-facing docs and screenshots
docker-compose.yml      # Quickstart compose
docker-compose.prod.yml # Production-shaped compose
nginx.conf              # Default nginx for the bundled client image
nginx.prod.conf         # TLS-terminating reverse-proxy config
```

Backend conventions:

- Controllers live in `web/`, services in `service/`, JPA entities in `domain/`, mappers in
  `mapper/`, security in `security/`, per-mock auth in `auth/`.
- DTOs are simple POJOs / Lombok-free where possible to keep tests cheap to write.
- Long-running side effects in services should be transactional and idempotent.
- All new code should hit ≥ 80% line coverage.

Frontend conventions:

- Components are presentational; HTTP work happens in services under `services/`.
- Use `HttpClientXsrfModule` rather than rolling your own CSRF token plumbing.
- Wrap backend errors with `ToastrService` for user feedback rather than `console.error`.

## Releasing

Maintainers cut releases via tagged commits on `main`. CI builds the multi-stage Docker images,
runs the test suite, and pushes versioned images to the configured registry.
