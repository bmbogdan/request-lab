package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import javax.inject.Inject

class GetHeaderSuggestionsUseCase @Inject constructor() {

    operator fun invoke(keyPrefix: String): List<String> {
        if (keyPrefix.isBlank()) return emptyList()
        return WELL_KNOWN_HEADERS.filter {
            it.startsWith(keyPrefix, ignoreCase = true)
        }
    }

    companion object {
        val WELL_KNOWN_HEADERS = listOf(
            "Accept",
            "Accept-Charset",
            "Accept-Encoding",
            "Accept-Language",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Content-Encoding",
            "Content-Length",
            "Content-Type",
            "Cookie",
            "Date",
            "Expect",
            "Host",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Unmodified-Since",
            "Origin",
            "Pragma",
            "Range",
            "Referer",
            "Transfer-Encoding",
            "User-Agent",
            "X-API-Key",
            "X-Auth-Token",
            "X-Forwarded-For",
            "X-Forwarded-Host",
            "X-Forwarded-Proto",
            "X-Request-ID",
            "X-Requested-With",
        )
    }
}
