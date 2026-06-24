package eu.mihaibadea.requestlab.feature.builder.data

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.IoDispatcher
import eu.mihaibadea.requestlab.core.common.runCatchingToAppResult
import eu.mihaibadea.requestlab.core.crypto.SecretCipher
import eu.mihaibadea.requestlab.feature.builder.data.dao.WorkingDraftDao
import eu.mihaibadea.requestlab.feature.builder.data.mapper.toDomain
import eu.mihaibadea.requestlab.feature.builder.data.mapper.toEntity
import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.model.DraftSource
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.builder.domain.model.emptyDraft
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestDraftRepositoryImpl @Inject constructor(
    private val workingDraftDao: WorkingDraftDao,
    private val cipher: SecretCipher,
    @IoDispatcher private val io: CoroutineDispatcher,
) : RequestDraftRepository {

    override suspend fun loadDraft(source: DraftSource): AppResult<RequestDraft> =
        runCatchingToAppResult {
            withContext(io) {
                val existing = workingDraftDao.get()
                if (existing != null) {
                    existing.toDomain(cipher)
                } else {
                    val newDraft = emptyDraft()
                    workingDraftDao.upsert(newDraft.toEntity(cipher))
                    newDraft
                }
            }
        }

    override suspend fun saveWorkingDraft(draft: RequestDraft): AppResult<Unit> =
        runCatchingToAppResult {
            withContext(io) {
                workingDraftDao.upsert(draft.toEntity(cipher))
            }
        }

    override fun observeWorkingDraft(): Flow<RequestDraft?> =
        workingDraftDao.observe().map { entity -> entity?.toDomain(cipher) }
}
