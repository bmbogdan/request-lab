# 5xx Server Errors

5xx codes indicate the server encountered an error or is unable to fulfil an otherwise valid request. The problem is on the server side.

## 500 Internal Server Error

A generic catch-all for unexpected server errors. The server encountered a condition that prevented it from fulfilling the request.

- Common causes: unhandled exception, database error, configuration problem
- Action: retry (the same request may succeed later), report to the API team

## 501 Not Implemented

The server does not support the functionality required to fulfil the request. Often returned for HTTP methods the server doesn't recognize.

- Common causes: calling a method the server is not yet implementing
- Different from 405: 501 means the server doesn't know how; 405 means it does but not at this URL

## 502 Bad Gateway

The server, while acting as a gateway or proxy, received an invalid response from an upstream server.

- Common causes: upstream service is down, nginx cannot reach the app server
- Often transient — retry after a few seconds

## 503 Service Unavailable

The server is temporarily unable to handle requests — due to overload or maintenance.

- Response may include: `Retry-After` header
- Common causes: deployment in progress, traffic spike, health check failure

## 504 Gateway Timeout

The server, acting as a gateway, did not receive a timely response from an upstream server.

- Common causes: slow database query, downstream service timeout, network partition
- Different from 502: 504 means the upstream was too slow; 502 means it responded with something invalid

## 507 Insufficient Storage

The server cannot store the representation needed to complete the request. Typically seen in WebDAV servers or APIs with storage quotas.

## Tips for handling 5xx

- **Retry with exponential backoff**: 500, 502, 503, and 504 are often transient.
- **Do not retry on 501**: the endpoint is not implemented and retrying won't help.
- **Log the request ID**: if the server returns `X-Request-ID` or similar, include it in bug reports so engineers can trace the exact failure.
