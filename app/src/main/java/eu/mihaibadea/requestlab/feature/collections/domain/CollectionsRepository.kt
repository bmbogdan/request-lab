package eu.mihaibadea.requestlab.feature.collections.domain

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.collections.domain.model.Collection
import eu.mihaibadea.requestlab.feature.collections.domain.model.SavedRequest
import eu.mihaibadea.requestlab.feature.collections.domain.model.SavedRequestDetail
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
