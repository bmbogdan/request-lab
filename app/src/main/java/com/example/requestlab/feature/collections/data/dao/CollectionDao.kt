package com.example.requestlab.feature.collections.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.requestlab.feature.collections.data.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

data class CollectionWithCountRow(
    val id: String,
    val name: String,
    val position: Int,
    val requestCount: Int,
)

@Dao
interface CollectionDao {

    @Query(
        """
        SELECT c.id, c.name, c.position, COUNT(r.id) AS requestCount
        FROM collections c
        LEFT JOIN saved_requests r ON r.collectionId = c.id
        GROUP BY c.id
        ORDER BY c.position ASC
        """,
    )
    fun observeAllWithCount(): Flow<List<CollectionWithCountRow>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: String): CollectionEntity?

    @Query("SELECT MAX(position) FROM collections")
    suspend fun getMaxPosition(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM collections")
    suspend fun clearAll()
}
