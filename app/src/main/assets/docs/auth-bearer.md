# Bearer Tokens

Bearer token authentication is the most common scheme for modern APIs. A token — typically a JWT — is included in the `Authorization` header of each request.

## The header format

```
Authorization: Bearer <token>
```

The word "Bearer" is the scheme name defined in RFC 6750. It signals that whoever holds ("bears") the token is authorized.

## JSON Web Tokens (JWT)

Most Bearer tokens are JWTs — a compact, self-contained format with three Base64URL-encoded parts separated by dots:

```
header.payload.signature
```

- **Header**: algorithm used to sign the token (e.g., `{"alg":"HS256","typ":"JWT"}`)
- **Payload**: claims — user ID, roles, expiration, issuer, etc.
- **Signature**: cryptographic proof the token hasn't been tampered with

You can inspect any JWT at jwt.io.

## Typical OAuth 2.0 flow

1. Client authenticates with the **authorization server** (login page, client credentials, etc.)
2. Server returns an **access token** (and optionally a **refresh token**)
3. Client includes the access token as a Bearer token in API requests
4. When the access token expires, the client uses the refresh token to get a new one

## Token expiry

Bearer tokens have an expiration time (`exp` claim in JWT). Common values:

- **Access token**: 15 minutes – 1 hour (short-lived)
- **Refresh token**: days to months (long-lived, stored securely)

## Security considerations

- **Never log the token** in plain text. Treat it like a password.
- **HTTPS is required** — a Bearer token intercepted over plain HTTP gives full API access.
- **Store securely**: on Android, use EncryptedSharedPreferences or Keystore-backed storage.
- Unlike cookies, Bearer tokens are not automatically sent — the client must attach them explicitly, which provides CSRF protection.

## Example request

```
GET /api/v1/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Server response on failure

```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="api", error="invalid_token"
```
