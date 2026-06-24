package com.example.requestlab.core.testing.fake

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.collections.domain.CollectionsRepository
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import com.example.requestlab.feature.collections.domain.model.SavedRequestDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCollectionsRepository : CollectionsRepository {

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    private val _requests = MutableStateFlow<List<SavedRequest>>(emptyList())

    fun setCollections(list: List<Collection>) { _collections.value = list }
    fun setRequests(list: List<SavedRequest>) { _requests.value = list }

    override fun observeCollections(): Flow<List<Collection>> = _collections.asStateFlow()

    override fun observeRequests(collectionId: String): Flow<List<SavedRequest>> =
        _requests.asStateFlow()

    override suspend fun createCollection(name: String): AppResult<String> {
        val id = "col-${_collections.value.size + 1}"
        _collections.value = _collections.value + Collection(id, name, 0, 0)
        return AppResult.Success(id)
    }

    override suspend fun renameCollection(id: String, name: String): AppResult<Unit> {
        _collections.value = _collections.value.map { if (it.id == id) it.copy(name = name) else it }
        return AppResult.Success(Unit)
    }

    override suspend fun deleteCollection(id: String): AppResult<Unit> {
        _collections.value = _collections.value.filter { it.id != id }
        return AppResult.Success(Unit)
    }

    override suspend fun saveRequest(
        collectionId: String,
        name: String,
        draft: RequestDraft,
    ): AppResult<String> = AppResult.Success("req-1")

    override suspend fun getRequestDetail(id: String): AppResult<SavedRequestDetail> =
        AppResult.Failure(com.example.requestlab.core.common.AppError.NotFound)

    override suspend fun reorder(collectionId: String, orderedIds: List<String>): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun removeRequest(id: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun clearAll(): AppResult<Unit> {
        _collections.value = emptyList()
        return AppResult.Success(Unit)
    }

    companion object {
        fun collection(id: String = "c1", name: String = "My Collection") =
            Collection(id, name, 0, 0)
    }
}
