# 2xx Success

2xx codes indicate the request was received, understood, and accepted.

## 200 OK

The standard success response. The response body contains the requested resource or the result of the action.

- GET: the resource is in the body
- POST (non-creating): the result is in the body
- PUT / PATCH: the updated resource may be in the body

## 201 Created

A new resource was successfully created. The `Location` header should point to the new resource's URL.

- Typical for: `POST /users` → `201 Created`, `Location: /users/99`
- Body: usually contains the created resource

## 202 Accepted

The request has been accepted for processing but processing is not complete yet. Used for async operations.

- Body: often contains a status URL or job ID to poll
- Example: `POST /exports` starts a background job

## 204 No Content

Success with no response body. Common for DELETE, or for PUT/PATCH when the server does not return the updated resource.

- Body: must be empty
- Typical for: `DELETE /users/42`, `PUT /settings`

## 206 Partial Content

The server is delivering only part of the resource due to a `Range` request header. Used for large file downloads and video streaming.

- Requires: `Content-Range` header in response
- Example: `GET /video.mp4` with `Range: bytes=0-1023`
