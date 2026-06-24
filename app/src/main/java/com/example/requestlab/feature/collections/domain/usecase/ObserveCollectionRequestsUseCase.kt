package com.example.requestlab.feature.collections.domain.usecase

import com.example.requestlab.feature.collections.domain.CollectionsRepository
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCollectionRequestsUseCase @Inject constructor(
    private val repository: CollectionsRepository,
) {
    operator fun invoke(collectionId: String): Flow<List<SavedRequest>> =
        repository.observeRequests(collectionId)
}
