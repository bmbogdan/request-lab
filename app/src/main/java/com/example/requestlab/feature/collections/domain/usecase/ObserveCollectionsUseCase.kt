package com.example.requestlab.feature.collections.domain.usecase

import com.example.requestlab.feature.collections.domain.CollectionsRepository
import com.example.requestlab.feature.collections.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCollectionsUseCase @Inject constructor(
    private val repo: CollectionsRepository,
) {
    operator fun invoke(): Flow<List<Collection>> = repo.observeCollections()
}
