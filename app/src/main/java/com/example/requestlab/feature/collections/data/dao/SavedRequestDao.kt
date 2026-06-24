package com.example.requestlab.feature.collections.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.requestlab.feature.collections.data.entity.SavedRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRequestDao {

    @Query("SELECT * FROM saved_requests WHERE collectionId = :collectionId ORDER BY position ASC")
    fun observeByCollection(collectionId: String): Flow<List<SavedRequestEntity>>

    @Query("SELECT * FROM saved_requests WHERE id = :id")
    suspend fun getById(id: String): SavedRequestEntity?

    @Query("SELECT MAX(position) FROM saved_requests WHERE collectionId = :collectionId")
    suspend fun getMaxPositionInCollection(collectionId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedRequestEntity)

    @Query("UPDATE saved_requests SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    @Query("DELETE FROM saved_requests WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saved_requests")
    suspend fun clearAll()
}
