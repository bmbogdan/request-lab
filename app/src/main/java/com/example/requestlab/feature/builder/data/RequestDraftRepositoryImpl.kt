package com.example.requestlab.feature.builder.data

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.IoDispatcher
import com.example.requestlab.core.common.runCatchingToAppResult
import com.example.requestlab.core.crypto.SecretCipher
import com.example.requestlab.feature.builder.data.dao.WorkingDraftDao
import com.example.requestlab.feature.builder.data.mapper.toDomain
import com.example.requestlab.feature.builder.data.mapper.toEntity
import com.example.requestlab.feature.builder.domain.RequestDraftRepository
import com.example.requestlab.feature.builder.domain.model.DraftSource
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.builder.domain.model.emptyDraft
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
