package com.example.requestlab.feature.builder.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.SendOutcome

interface SendRepository {
    suspend fun send(prepared: PreparedRequest): AppResult<SendOutcome>
}
