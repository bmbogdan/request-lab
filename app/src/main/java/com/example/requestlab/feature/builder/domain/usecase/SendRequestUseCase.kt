package com.example.requestlab.feature.builder.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.feature.builder.domain.SendRepository
import javax.inject.Inject

class SendRequestUseCase @Inject constructor(
    private val sendRepository: SendRepository,
) {
    suspend operator fun invoke(prepared: PreparedRequest): AppResult<SendOutcome> =
        sendRepository.send(prepared)
}
