# Response Headers

Response headers carry metadata about the server's response — content format, caching rules, redirects, and more.

## Content-Type

Describes the media type and encoding of the response body.

- `Content-Type: application/json; charset=utf-8`
- `Content-Type: text/html; charset=utf-8`
- `Content-Type: image/png`

Always check this before parsing the body — a 200 response can still be HTML if the server returned an error page.

## Content-Length

The size of the response body in bytes.

- `Content-Length: 2048`

Not present when the server uses chunked transfer encoding (`Transfer-Encoding: chunked`).

## Content-Encoding

The compression applied to the response body.

- `Content-Encoding: gzip`
- `Content-Encoding: br` (Brotli)

HTTP clients decompress automatically when this header is present.

## Cache-Control

Directives for caching the response.

- `Cache-Control: no-store` — never cache
- `Cache-Control: no-cache` — revalidate before using cached copy
- `Cache-Control: max-age=3600` — cache for 1 hour
- `Cache-Control: private, max-age=300` — user-specific, cache 5 min
- `Cache-Control: public, max-age=86400` — shared cache OK, 1 day

## ETag

An opaque identifier for the current version of a resource. Send back in `If-None-Match` for efficient cache revalidation.

- `ETag: "33a64df551425fcc55e4d42a148795d9f25f89d"`

## Location

URL the client should redirect to (used with 3xx responses) or the URL of a newly created resource (used with 201 Created).

- `Location: https://api.example.com/users/99`

## WWW-Authenticate

Sent with a 401 response to tell the client which authentication scheme to use.

- `WWW-Authenticate: Bearer realm="api"`
- `WWW-Authenticate: Basic realm="My App"`

## Retry-After

Tells the client how long to wait before retrying. Used with 429 Too Many Requests and 503 Service Unavailable.

- `Retry-After: 60` (seconds)
- `Retry-After: Wed, 21 Jun 2024 12:00:00 GMT`

## X-RateLimit-*

Non-standard but common headers for communicating rate limit status.

- `X-RateLimit-Limit: 1000`
- `X-RateLimit-Remaining: 42`
- `X-RateLimit-Reset: 1718960400`

## CORS Headers

Required for cross-origin requests to succeed in browsers.

- `Access-Control-Allow-Origin: *` — any origin
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE`
- `Access-Control-Allow-Headers: Content-Type, Authorization`
- `Access-Control-Max-Age: 86400` — preflight cache duration
