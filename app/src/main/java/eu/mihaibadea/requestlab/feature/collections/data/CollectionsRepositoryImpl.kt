package eu.mihaibadea.requestlab.feature.collections.data

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.IoDispatcher
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.runCatchingToAppResult
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.collections.data.dao.CollectionDao
import eu.mihaibadea.requestlab.feature.collections.data.dao.SavedRequestDao
import eu.mihaibadea.requestlab.feature.collections.data.entity.CollectionEntity
import eu.mihaibadea.requestlab.feature.collections.data.mapper.toDomain
import eu.mihaibadea.requestlab.feature.collections.data.mapper.toDetail
import eu.mihaibadea.requestlab.feature.collections.data.mapper.toSummary
import eu.mihaibadea.requestlab.feature.collections.data.mapper.toSavedRequestEntity
import eu.mihaibadea.requestlab.feature.collections.domain.CollectionsRepository
import eu.mihaibadea.requestlab.feature.collections.domain.model.Collection
import eu.mihaibadea.requestlab.feature.collections.domain.model.SavedRequest
import eu.mihaibadea.requestlab.feature.collections.domain.model.SavedRequestDetail
import eu.mihaibadea.requestlab.feature.settings.data.AppResetCoordinator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionsRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val savedRequestDao: SavedRequestDao,
    coordinator: AppResetCoordinator,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CollectionsRepository {

    init {
        // FK CASCADE on saved_requests.collectionId handles cascade deletes
        coordinator.clearCollections = { collectionDao.clearAll() }
    }

    override fun observeCollections(): Flow<List<Collection>> =
        collectionDao.observeAllWithCount().map { rows -> rows.map { it.toDomain() } }

    override fun observeRequests(collectionId: String): Flow<List<SavedRequest>> =
        savedRequestDao.observeByCollection(collectionId).map { entities -> entities.map { it.toSummary() } }

    override suspend fun createCollection(name: String): AppResult<String> =
        runCatchingToAppResult {
            withContext(io) {
                val id = UUID.randomUUID().toString()
                val position = (collectionDao.getMaxPosition() ?: -1) + 1
                collectionDao.insert(CollectionEntity(id, name, position))
                id
            }
        }

    override suspend fun renameCollection(id: String, name: String): AppResult<Unit> =
        runCatchingToAppResult {
            withContext(io) {
                val existing = collectionDao.getById(id) ?: throw NoSuchElementException(id)
                collectionDao.insert(existing.copy(name = name))
            }
        }

    override suspend fun deleteCollection(id: String): AppResult<Unit> =
        runCatchingToAppResult { withContext(io) { collectionDao.deleteById(id) } }

    override suspend fun saveRequest(
        collectionId: String,
        name: String,
        draft: RequestDraft,
    ): AppResult<String> =
        runCatchingToAppResult {
            withContext(io) {
                val id = UUID.randomUUID().toString()
                val position = (savedRequestDao.getMaxPositionInCollection(collectionId) ?: -1) + 1
                savedRequestDao.insert(draft.toSavedRequestEntity(id, collectionId, name, position))
                id
            }
        }

    override suspend fun getRequestDetail(id: String): AppResult<SavedRequestDetail> =
        runCatchingToAppResult {
            withContext(io) {
                savedRequestDao.getById(id)?.toDetail() ?: throw NoSuchElementException(id)
            }
        }

    override suspend fun reorder(collectionId: String, orderedIds: List<String>): AppResult<Unit> =
        runCatchingToAppResult {
            withContext(io) {
                orderedIds.forEachIndexed { index, id -> savedRequestDao.updatePosition(id, index) }
            }
        }

    override suspend fun removeRequest(id: String): AppResult<Unit> =
        runCatchingToAppResult { withContext(io) { savedRequestDao.deleteById(id) } }

    override suspend fun clearAll(): AppResult<Unit> =
        runCatchingToAppResult { withContext(io) { collectionDao.clearAll() } }
}
