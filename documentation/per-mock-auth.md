# Per-mock authentication guide

Mockpit 2.0 lets you protect each mock with the authentication scheme of your choice. This guide
walks through every supported scheme with curl examples so you can verify behaviour end-to-end.

> All examples assume Mockpit is reachable at `https://mockpit.example.com`. Replace with your
> deployment's hostname.

## Configuring auth on a mock

1. Sign in to the Mockpit UI.
2. Open or create a mock.
3. Scroll to the **Authentication** section.
4. Choose a scheme from the dropdown.
5. Fill in the per-scheme inputs (see sections below).
6. (Optional) Customise the **failure response** — status code, content-type and body that
   Mockpit returns when authentication fails.
7. (Optional) Use the **Test auth** button to dry-run a sample request.
8. Save the mock.

The scheme is stored alongside the mock; secrets (HMAC keys, OAuth2 client secrets, PEM material)
are encrypted with AES-256-GCM before being written to the database.

## None

Default. The mock is publicly accessible:

```bash
curl https://mockpit.example.com/api/users/42
```

## Basic

Username + password. Passwords are hashed with BCrypt before persistence.

Configure:
- Username: `alice`
- Password: `CorrectHorseBatteryStaple`
- Realm: `mockpit` (defaults to `mockpit` if blank)

Request:
```bash
curl -u alice:CorrectHorseBatteryStaple https://mockpit.example.com/api/users/42
```

Failure (`401 Unauthorized`):
```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="mockpit"
{"error":"unauthorized","message":"Invalid Basic credentials."}
```

## JWT (Bearer)

Verify a JSON Web Token signed with HS256/384/512, RS256/384/512 or ES256/384/512.

### HMAC (HS\*)

Configure:
- Algorithm: `HS256`
- Shared secret: `my-32-bytes-or-more-of-random-data`
- Required issuer: `https://issuer.example.com` (optional)
- Required audiences: `[ "api" ]` (optional)
- Required scopes: `[ "read", "write" ]` (optional)
- Required claims: `{ "tenant": "acme" }` (optional key/value)
- Clock skew: `60` seconds (default)

Request:
```bash
TOKEN=$(jwt encode \
    --secret 'my-32-bytes-or-more-of-random-data' \
    --iss https://issuer.example.com \
    --aud api \
    --exp +1h \
    --payload scope='read write')
curl -H "Authorization: Bearer $TOKEN" https://mockpit.example.com/api/users/42
```

### Asymmetric (RS\*, ES\*)

Configure either:
- A **JWKS URI** (preferred for OIDC providers — Mockpit caches the JWKS for 10 minutes), or
- A static **PEM-encoded public key** (paste the `-----BEGIN PUBLIC KEY-----` block).

Mockpit verifies the signature, the configured `iss`/`aud`/`exp`/`scope`/custom claims, and
rejects any token whose `alg` header doesn't match the configured algorithm (algorithm-confusion
defence).

## OAuth2 Resource Server

Validate access tokens issued by an OIDC provider without managing the JWKS yourself.

Configure:
- Issuer: `https://your.auth0.com/` (or any OIDC issuer)
- JWKS URI: leave blank to discover from `/.well-known/openid-configuration`, or set explicitly
- Audiences: `[ "https://mockpit.example.com/api" ]`
- Scopes: `[ "openid", "profile" ]`

Request (token issued by your IdP):
```bash
ACCESS_TOKEN=$(curl -s -X POST https://your.auth0.com/oauth/token \
    -H 'Content-Type: application/json' \
    -d '{
        "client_id":"...",
        "client_secret":"...",
        "audience":"https://mockpit.example.com/api",
        "grant_type":"client_credentials"
    }' | jq -r .access_token)

curl -H "Authorization: Bearer $ACCESS_TOKEN" https://mockpit.example.com/api/users/42
```

## OAuth2 Introspection (RFC 7662)

For opaque tokens. Mockpit POSTs the token to your introspection endpoint with its client
credentials and accepts the response when `active=true`.

Configure:
- Introspection URI: `https://your.auth.com/oauth2/introspect`
- Client ID: `mockpit`
- Client secret: `s3cret`
- Required scopes: `[ "read" ]` (optional)
- Required audiences: `[ "api" ]` (optional)
- Cache TTL: `60` seconds — positive responses are cached to avoid hammering the AS

Request:
```bash
curl -H "Authorization: Bearer some-opaque-token" \
    https://mockpit.example.com/api/users/42
```

## Custom failure response

Override the default 401 / 403 envelope with your own:

- Status: `403`
- Content-Type: `application/json`
- Body:
```json
{ "error": "Sign in to your account first.", "loginUrl": "https://example.com/login" }
```

The configured `WWW-Authenticate` challenge is still returned alongside your custom body so
clients can negotiate.

## SSRF defences

Outbound calls to your JWKS / OIDC discovery / introspection endpoints go through a hardened
HTTP client that:

- Rejects schemes other than `http` or `https`.
- Resolves the host and rejects any address in RFC 1918, loopback, link-local, multicast, or the
  cloud metadata IP `169.254.169.254`.
- Honours an optional allowlist (`MOCKPIT_ALLOWED_HOSTS`) — when set, only these hostnames may
  be contacted.
- Disables redirect-following.
- Caps response size at 1 MiB and applies short timeouts.

You can lock down outbound auth calls by setting:

```yaml
mockpit:
  http-client:
    allowed-hosts:
      - your.auth0.com
      - your.idp.example.com
```

## Operational notes

- Per-IP rate limiting (Bucket4j) is enforced on both admin and live surfaces. Adjust via
  `MOCKPIT_RATELIMIT_LIVE` / `MOCKPIT_RATELIMIT_ADMIN`.
- Stack traces never leak in non-`dev` profiles; auth-failure responses log the underlying reason
  server-side via the `LiveService` logger so you can debug from access logs.
- Rotate `MOCKPIT_SECRET_CIPHER_KEY` carefully: re-encrypt all `auth_config_json` rows with the
  new key before swapping. The cipher refuses to decrypt blobs sealed with a different key.
