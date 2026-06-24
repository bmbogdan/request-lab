package com.example.requestlab.feature.builder.domain.usecase

import javax.inject.Inject

class GetHeaderValueSuggestionsUseCase @Inject constructor() {

    operator fun invoke(headerKey: String): List<String> =
        HEADER_VALUE_MAP[headerKey.lowercase()] ?: emptyList()

    companion object {
        private val HEADER_VALUE_MAP = mapOf(
            "accept" to listOf(
                "application/json",
                "application/xml",
                "text/html",
                "text/plain",
                "*/*",
            ),
            "content-type" to listOf(
                "application/json",
                "application/x-www-form-urlencoded",
                "multipart/form-data",
                "text/plain",
                "text/html",
                "application/xml",
                "application/octet-stream",
            ),
            "accept-encoding" to listOf(
                "gzip",
                "deflate",
                "br",
                "identity",
                "gzip, deflate, br",
            ),
            "accept-language" to listOf(
                "en-US",
                "en-US,en;q=0.9",
                "*",
            ),
            "cache-control" to listOf(
                "no-cache",
                "no-store",
                "max-age=0",
                "must-revalidate",
                "public",
                "private",
            ),
            "connection" to listOf(
                "keep-alive",
                "close",
            ),
            "authorization" to listOf(
                "Bearer ",
                "Basic ",
            ),
        )
    }
}
