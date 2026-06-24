# Query Parameters

Query parameters (also called query strings) pass key-value data to the server as part of the URL, after the `?` character.

## Anatomy of a URL with query parameters

```
https://api.example.com/search?q=android&page=2&limit=20
```

- `?` separates the path from the query string
- `q=android` is the first parameter
- `&` separates parameters
- `page=2` and `limit=20` are additional parameters

## URL encoding

Parameter values must be percent-encoded (URL-encoded) when they contain characters outside the safe ASCII set.

Common encodings:

- Space → `%20` or `+`
- `&` → `%26`
- `=` → `%3D`
- `#` → `%23`
- `/` → `%2F`
- `:` → `%3A`
- `+` → `%2B`

Example: searching for `C++ & Java` becomes `q=C%2B%2B+%26+Java`.

## Common uses

- **Filtering**: `GET /products?category=shoes&color=red`
- **Pagination**: `GET /users?page=3&per_page=25`
- **Sorting**: `GET /posts?sort=created_at&order=desc`
- **Search**: `GET /search?q=query+string`
- **Format selection**: `GET /data?format=json`

## Query params vs path params

| | Query params | Path params |
|---|---|---|
| Location | `?key=value` | `/users/{id}` |
| Use for | Filters, pagination, options | Resource identity |
| Optional? | Often optional | Usually required |
| Example | `?limit=10` | `/users/42` |

## Duplicate keys

Some APIs accept the same key multiple times to represent a list:

```
GET /filter?tag=android&tag=kotlin&tag=compose
```

Others use bracket notation (`tag[]=android&tag[]=kotlin`) or comma-separated values (`tags=android,kotlin`). Check the API's documentation for the expected format.

## Sensitive data

Avoid placing sensitive information (passwords, tokens, PII) in query parameters — they appear in:

- Server access logs
- Browser history
- HTTP `Referer` headers passed to third-party resources
- Proxy and CDN logs

Use request headers or a POST body for sensitive data instead.
