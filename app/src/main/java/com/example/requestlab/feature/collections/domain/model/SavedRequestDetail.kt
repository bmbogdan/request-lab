package com.example.requestlab.feature.collections.domain.model

import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.RequestBody

data class SavedRequestDetail(
    val savedRequest: SavedRequest,
    val headers: List<KeyValue>,
    val params: List<KeyValue>,
    val body: RequestBody,
    val auth: AuthConfig,
)
