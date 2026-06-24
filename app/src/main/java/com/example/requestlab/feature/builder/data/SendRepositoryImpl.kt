package com.example.requestlab.feature.builder.data

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.ConnectivityObserver
import com.example.requestlab.core.common.IoDispatcher
import com.example.requestlab.core.common.getOrNull
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.RequestConfig
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.core.network.HttpEngine
import com.example.requestlab.core.network.RawHttpResult
import com.example.requestlab.feature.builder.domain.SendRepository
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import com.example.requestlab.feature.history.domain.HistoryRepository
import com.example.requestlab.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendRepositoryImpl @Inject constructor(
    private val engine: HttpEngine,
    private val connectivityObserver: ConnectivityObserver,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val environmentsRepository: EnvironmentsRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SendRepository {

    override suspend fun send(prepared: PreparedRequest): AppResult<SendOutcome> {
        if (!connectivityObserver.isConnected()) {
            return AppResult.Failure(AppError.NoInternet)
        }

        val settings = settingsRepository.observeSettings().first()
        val updatedPrepared = prepared.copy(
            config = RequestConfig(
                timeoutSeconds = settings.timeoutSeconds,
                followRedirects = settings.followRedirects,
            ),
        )

        val sentAt = Instant.now()
        val rawResult = withContext(io) { engine.execute(updatedPrepared) }

        val outcome = when (rawResult) {
            is RawHttpResult.Success -> SendOutcome(
                request = updatedPrepared,
                response = rawResult.response,
                failure = null,
                sentAt = sentAt,
            )
            is RawHttpResult.TransportError -> SendOutcome(
                request = updatedPrepared,
                response = null,
                failure = rawResult.failure,
                sentAt = sentAt,
            )
        }

        val activeEnvId = environmentsRepository.observeActiveEnvironmentId().first()
        val environmentName = if (activeEnvId != null) {
            environmentsRepository.getDetail(activeEnvId).getOrNull()?.name
        } else null

        historyRepository.record(outcome, environmentName)

        return AppResult.Success(outcome)
    }
}
