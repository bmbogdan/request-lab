package eu.mihaibadea.requestlab.feature.environments.data

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.IoDispatcher
import eu.mihaibadea.requestlab.core.common.runCatchingToAppResult
import eu.mihaibadea.requestlab.core.crypto.SecretCipher
import eu.mihaibadea.requestlab.core.datastore.SettingsDataStore
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentDao
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentVariableDao
import eu.mihaibadea.requestlab.feature.environments.data.entity.EnvironmentEntity
import eu.mihaibadea.requestlab.feature.environments.data.mapper.toDomain
import eu.mihaibadea.requestlab.feature.environments.data.mapper.toEntity
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.model.Environment
import eu.mihaibadea.requestlab.feature.environments.domain.model.EnvironmentDetail
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentsRepositoryImpl @Inject constructor(
    private val environmentDao: EnvironmentDao,
    private val variableDao: EnvironmentVariableDao,
    private val cipher: SecretCipher,
    private val dataStore: SettingsDataStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : EnvironmentsRepository {

    override fun observeEnvironments(): Flow<List<Environment>> =
        combine(environmentDao.observeWithCounts(), dataStore.activeEnvironmentId) { envs, activeId ->
            envs.map { it.toDomain(activeId) }
        }

    override fun observeActiveEnvironmentId(): Flow<String?> = dataStore.activeEnvironmentId

    override suspend fun setActive(id: String?): AppResult<Unit> = runCatchingToAppResult {
        withContext(io) { dataStore.setActiveEnvironmentId(id) }
    }

    override suspend fun getDetail(id: String): AppResult<EnvironmentDetail> = runCatchingToAppResult {
        withContext(io) {
            val entity = environmentDao.getById(id) ?: throw NoSuchElementException("Environment $id not found")
            val vars = variableDao.getForEnvironment(id).map { it.toDomain(cipher) }
            EnvironmentDetail(id = entity.id, name = entity.name, variables = vars)
        }
    }

    override suspend fun create(name: String): AppResult<String> = runCatchingToAppResult {
        val id = UUID.randomUUID().toString()
        withContext(io) {
            environmentDao.upsert(
                EnvironmentEntity(id = id, name = name, createdAt = System.currentTimeMillis()),
            )
        }
        id
    }

    override suspend fun rename(id: String, name: String): AppResult<Unit> = runCatchingToAppResult {
        withContext(io) { environmentDao.rename(id, name) }
    }

    override suspend fun delete(id: String): AppResult<Unit> = runCatchingToAppResult {
        withContext(io) {
            environmentDao.delete(id)
            val activeId = dataStore.activeEnvironmentId.first()
            if (activeId == id) dataStore.setActiveEnvironmentId(null)
        }
    }

    override suspend fun saveVariables(id: String, variables: List<Variable>): AppResult<Unit> =
        runCatchingToAppResult {
            withContext(io) {
                variableDao.replaceAll(id, variables.map { it.toEntity(id, cipher) })
            }
        }

    override suspend fun resolvedVariables(id: String): AppResult<Map<String, String>> =
        runCatchingToAppResult {
            withContext(io) {
                variableDao.getForEnvironment(id)
                    .associate { it.key to cipher.decrypt(it.valueCipher) }
            }
        }
}
