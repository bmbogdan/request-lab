package com.example.requestlab.feature.collections.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import com.example.requestlab.feature.collections.domain.model.SavedRequestDetail
import kotlinx.coroutines.flow.Flow

interface CollectionsRepository {
    fun observeCollections(): Flow<List<Collection>>
    fun observeRequests(collectionId: String): Flow<List<SavedRequest>>
    suspend fun createCollection(name: String): AppResult<String>
    suspend fun renameCollection(id: String, name: String): AppResult<Unit>
    suspend fun deleteCollection(id: String): AppResult<Unit>
    suspend fun saveRequest(collectionId: String, name: String, draft: RequestDraft): AppResult<String>
    suspend fun getRequestDetail(id: String): AppResult<SavedRequestDetail>
    suspend fun reorder(collectionId: String, orderedIds: List<String>): AppResult<Unit>
    suspend fun removeRequest(id: String): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
}
