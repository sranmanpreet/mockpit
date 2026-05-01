# Security Policy

## Supported versions

| Version | Supported          |
| ------- | ------------------ |
| 2.x     | Yes                |
| 1.x     | Critical fixes only — please upgrade. |

## Reporting a vulnerability

If you believe you have found a security issue in Mockpit, **please do not open a public GitHub
issue**. Instead, email the maintainers at `security@mockpit.dev` (substitute the address
configured for your fork) with:

- A description of the vulnerability.
- Steps to reproduce, ideally with a minimal proof-of-concept.
- The affected version(s).
- Any mitigating factors you are aware of.

We will acknowledge receipt within 3 business days and aim to ship a fix or mitigation within 30
days for high-severity issues. We are happy to credit reporters in the release notes if you
would like.

## What's in scope

In-scope examples:

- Authentication / authorization bypasses on either the admin API or live mock endpoints.
- Injection attacks (SQL, JS sandbox escape, log injection, etc.).
- Server-side request forgery via JWKS / OIDC discovery / introspection requests.
- Cross-site scripting in the SPA.
- CSRF on state-changing admin endpoints.
- Deserialisation attacks against the mock import endpoint.
- Information disclosure via error responses.

Out-of-scope:

- Issues requiring physical access or privileged access on the host.
- Self-XSS without an external attacker vector.
- Reports about missing best-practice headers on endpoints that already serve no sensitive
  content (we will gladly accept hardening PRs but it isn't a vulnerability).
- Findings against deployments running with `SPRING_PROFILES_ACTIVE != prod`.

## Hardening already in place (2.0)

- BCrypt-hashed passwords (admin accounts and per-mock Basic auth).
- AES-256-GCM encryption-at-rest for per-mock secrets.
- JWT-in-HttpOnly+Secure cookie sessions for the admin SPA + CSRF tokens for state-changing endpoints.
- Sandboxed GraalJS execution: HostAccess.NONE, no IO/threads/native, statement count limit,
  wall-clock watchdog, output cap.
- SSRF defence on outbound auth calls (scheme allowlist, RFC1918 / loopback / metadata blocking,
  optional host allowlist, redirect-following disabled).
- Strict CORS allowlist (no wildcards in prod).
- HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy and Permissions-Policy
  on every response.
- Per-IP token-bucket rate limiting (admin and live).
- Stack traces never leak to API clients.
- Multi-tenant ownership checks on every mock fetch (no IDOR).
- Reserved-path denylist preventing user mocks from shadowing internal endpoints.
- Mandatory minimum entropy for secrets (≥ 32 bytes for JWT signing, exactly 32 bytes for the
  AES-GCM cipher key); the application refuses to start if either is misconfigured in prod.

## Reporting a non-vulnerability

For general bugs, feature requests, or hardening suggestions please open a normal GitHub issue.
