package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConnectivityUseCase @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
) {
    operator fun invoke(): Flow<Boolean> = connectivityObserver.observe()
}
