# Request Headers

Request headers let the client pass additional information to the server alongside the request.

## Content-Type

Tells the server the media type of the request body.

- `Content-Type: application/json` — JSON body
- `Content-Type: application/x-www-form-urlencoded` — HTML form data
- `Content-Type: multipart/form-data` — file uploads
- `Content-Type: text/plain` — plain text

Required when sending a body with POST, PUT, or PATCH.

## Authorization

Carries credentials for authenticating the request.

- `Authorization: Basic <base64(user:pass)>` — HTTP Basic Auth
- `Authorization: Bearer <token>` — Bearer token (OAuth 2.0, JWT)
- `Authorization: ApiKey <key>` — custom API key scheme (non-standard)

## Accept

Tells the server which content types the client can handle.

- `Accept: application/json` — prefer JSON
- `Accept: text/html, application/json` — HTML or JSON
- `Accept: */*` — anything (the default)

## Accept-Encoding

Lists the compression algorithms the client supports.

- `Accept-Encoding: gzip, deflate, br`

Servers that support compression will encode the response body and add `Content-Encoding` to the response.

## User-Agent

Identifies the client software making the request.

- `User-Agent: MyApp/2.1 (Android 14; Pixel 8)`

## Cache-Control (request)

Controls caching behavior for this specific request.

- `Cache-Control: no-cache` — revalidate with the origin server before using a cached response
- `Cache-Control: no-store` — do not cache the response
- `Cache-Control: max-age=0` — treat all cached responses as stale

## If-None-Match

Used for conditional requests. The server only sends the full response if the resource's `ETag` has changed.

- `If-None-Match: "abc123"` — return 304 if ETag matches

## If-Modified-Since

Returns 304 Not Modified if the resource has not changed since the given date.

- `If-Modified-Since: Wed, 21 Jun 2024 07:28:00 GMT`

## X-Request-ID / X-Correlation-ID

Custom headers for tracing requests through distributed systems. Not standardized but widely used.

- `X-Request-ID: 550e8400-e29b-41d4-a716-446655440000`
