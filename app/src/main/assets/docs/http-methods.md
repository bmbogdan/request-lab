# HTTP Methods

HTTP defines a set of request methods that indicate the desired action for a resource. Each method has specific semantics around safety and idempotency.

## GET

Retrieves a resource. Should have no side effects on the server — it is both **safe** and **idempotent**.

- Use for: fetching data, reading resources
- Body: not recommended (some servers ignore it)
- Example: `GET /users/42`

## POST

Submits data to create a new resource or trigger a server action. **Not safe, not idempotent** — calling it twice may create two resources.

- Use for: creating resources, submitting forms, RPC calls
- Body: required, typically JSON or form data
- Example: `POST /users`

## PUT

Replaces a resource at a specific URL entirely. **Idempotent** — calling it twice produces the same result.

- Use for: full replacement of a resource
- Body: the complete new representation
- Example: `PUT /users/42`

## PATCH

Partially updates an existing resource. May or may not be idempotent depending on implementation.

- Use for: partial updates (change only the fields you specify)
- Body: the fields to change, often in JSON Merge Patch or JSON Patch format
- Example: `PATCH /users/42`

## DELETE

Removes a resource. **Idempotent** — deleting an already-deleted resource should return 204 or 404, not an error.

- Use for: removing resources
- Body: usually empty
- Example: `DELETE /users/42`

## HEAD

Same as GET but the server returns only headers, no body. Useful for checking whether a resource exists or inspecting headers without downloading the full response.

- Use for: checking resource existence, cache validation
- Example: `HEAD /files/large.zip`

## OPTIONS

Returns the HTTP methods supported by a URL. Browsers send a preflight OPTIONS request before cross-origin requests (CORS).

- Use for: CORS preflight, API capability discovery
- Response includes: `Allow` header listing supported methods
