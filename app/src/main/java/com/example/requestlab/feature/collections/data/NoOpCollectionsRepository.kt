package com.example.requestlab.feature.collections.data

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.collections.domain.CollectionsRepository
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import com.example.requestlab.feature.collections.domain.model.SavedRequestDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpCollectionsRepository @Inject constructor() : CollectionsRepository {
    override fun observeCollections(): Flow<List<Collection>> = flowOf(emptyList())
    override fun observeRequests(collectionId: String): Flow<List<SavedRequest>> = flowOf(emptyList())
    override suspend fun createCollection(name: String): AppResult<String> = AppResult.Failure(AppError.NotFound)
    override suspend fun renameCollection(id: String, name: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun deleteCollection(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun saveRequest(collectionId: String, name: String, draft: RequestDraft): AppResult<String> =
        AppResult.Failure(AppError.NotFound)
    override suspend fun getRequestDetail(id: String): AppResult<SavedRequestDetail> = AppResult.Failure(AppError.NotFound)
    override suspend fun reorder(collectionId: String, orderedIds: List<String>): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun removeRequest(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun clearAll(): AppResult<Unit> = AppResult.Success(Unit)
}
