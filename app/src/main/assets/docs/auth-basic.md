# Basic Authentication

HTTP Basic Authentication transmits credentials as a Base64-encoded string in the `Authorization` header.

## How it works

1. Combine username and password with a colon: `user:password`
2. Base64-encode the result: `dXNlcjpwYXNzd29yZA==`
3. Send in the header: `Authorization: Basic dXNlcjpwYXNzd29yZA==`

The server decodes the header, splits on `:`, and checks the credentials.

## Important security notes

- **Base64 is not encryption.** Anyone who intercepts the header can decode it immediately.
- **Always use HTTPS.** Without TLS, credentials are sent in plain text over the network.
- **Credentials are sent with every request.** There is no session — each request must include the header.

## When to use Basic Auth

- Internal tools behind a VPN or on a trusted network
- Simple API access where simplicity matters more than sophistication
- Development and testing environments
- Machine-to-machine requests where the client is trusted

## When not to use Basic Auth

- User-facing login flows (prefer OAuth 2.0 or session cookies)
- Any situation where you cannot guarantee HTTPS
- High-value or regulated data (PCI-DSS, HIPAA) — use stronger schemes

## Example

```
Username: alice
Password: s3cr3t
Encoded: YWxpY2U6czNjcjN0
Header:  Authorization: Basic YWxpY2U6czNjcjN0
```

## Server response on failure

If Basic Auth fails, the server returns:

```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="My API"
```

The `realm` value is a human-readable description of the protected area.
