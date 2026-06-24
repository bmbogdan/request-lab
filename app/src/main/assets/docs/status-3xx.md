# 3xx Redirection

3xx codes tell the client to take additional action, usually following a redirect to a different URL given in the `Location` header.

## 301 Moved Permanently

The resource has permanently moved to the URL in `Location`. Clients and search engines should update their links. **The method may change to GET** on redirect (historical behaviour).

- Use: permanent URL changes
- Safe for: bookmarks, SEO

## 302 Found

Temporary redirect. The resource is currently at the `Location` URL but may return to the original URL in future. **The method may change to GET** on redirect.

- Use: temporary maintenance pages, A/B testing
- Do not cache unless `Cache-Control` says so

## 303 See Other

Redirect to a GET request regardless of the original method. Used after a POST to redirect to the result page, avoiding duplicate submissions on refresh.

- Pattern: POST form → 303 → GET confirmation page

## 304 Not Modified

The resource has not changed since the version specified by `If-None-Match` or `If-Modified-Since`. The client should use its cached copy. **No body**.

- Requires: prior GET with ETag or Last-Modified
- Saves bandwidth by skipping the response body

## 307 Temporary Redirect

Temporary redirect where **the method and body must not change**. Use instead of 302 when you need to preserve POST.

- Use: redirect a POST to another endpoint temporarily

## 308 Permanent Redirect

Permanent redirect where **the method and body must not change**. The permanent equivalent of 307.

- Use: permanent URL change when the method must be preserved (e.g., moving a POST endpoint)
