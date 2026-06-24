# 4xx Client Errors

4xx codes indicate the request was invalid or could not be fulfilled due to a problem with the client's request.

## 400 Bad Request

The server could not understand the request due to malformed syntax, invalid parameters, or a missing required field.

- Common causes: invalid JSON body, missing required field, wrong parameter type
- Fix: check the request body and parameters

## 401 Unauthorized

Authentication is required and has failed or not been provided. Despite the name, it means **unauthenticated**.

- Response includes: `WWW-Authenticate` header
- Fix: add or refresh credentials

## 403 Forbidden

The client is authenticated but does not have permission to access the resource. Unlike 401, re-authenticating will not help.

- Common causes: insufficient role, IP restriction, resource ownership check failed
- Fix: check authorization, not authentication

## 404 Not Found

The requested resource does not exist at this URL. Can also be used intentionally to hide a resource from unauthorised clients (instead of 403).

- Common causes: wrong ID, deleted resource, typo in URL

## 405 Method Not Allowed

The HTTP method used is not supported for this endpoint. The response should include an `Allow` header listing valid methods.

- Example: `PUT /login` on an endpoint that only accepts POST

## 409 Conflict

The request conflicts with the current state of the resource. Common in optimistic concurrency control or when creating a duplicate.

- Common causes: duplicate username/email, version mismatch, concurrent edit conflict

## 410 Gone

The resource existed previously but has been permanently removed. Unlike 404, 410 signals the removal was intentional and permanent.

- Use: deleted content that should be de-indexed by search engines

## 422 Unprocessable Entity

The request is well-formed (syntactically valid) but the contained data fails validation rules.

- Common causes: business rule violation, invalid field value, referential integrity error
- Often includes a body describing which fields failed and why

## 429 Too Many Requests

The client has sent too many requests in a given time window (rate limiting).

- Response includes: `Retry-After` header, sometimes `X-RateLimit-*` headers
- Fix: back off and retry after the indicated delay
