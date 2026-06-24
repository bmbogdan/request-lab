package eu.mihaibadea.requestlab.feature.environments.domain

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.environments.domain.model.Environment
import eu.mihaibadea.requestlab.feature.environments.domain.model.EnvironmentDetail
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable
import kotlinx.coroutines.flow.Flow

interface EnvironmentsRepository {
    fun observeEnvironments(): Flow<List<Environment>>
    fun observeActiveEnvironmentId(): Flow<String?>
    suspend fun setActive(id: String?): AppResult<Unit>
    suspend fun getDetail(id: String): AppResult<EnvironmentDetail>
    suspend fun create(name: String): AppResult<String>
    suspend fun rename(id: String, name: String): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun saveVariables(id: String, variables: List<Variable>): AppResult<Unit>
    suspend fun resolvedVariables(id: String): AppResult<Map<String, String>>
}
