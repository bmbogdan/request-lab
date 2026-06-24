package com.example.requestlab.feature.environments.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.requestlab.feature.environments.data.entity.EnvironmentEntity
import com.example.requestlab.feature.environments.data.entity.EnvironmentWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentDao {

    @Query(
        """SELECT e.id, e.name,
           (SELECT COUNT(*) FROM environment_variable v WHERE v.environmentId = e.id) AS variableCount
           FROM environment e ORDER BY e.name""",
    )
    fun observeWithCounts(): Flow<List<EnvironmentWithCount>>

    @Query("SELECT * FROM environment WHERE id = :id")
    suspend fun getById(id: String): EnvironmentEntity?

    @Upsert
    suspend fun upsert(env: EnvironmentEntity)

    @Query("UPDATE environment SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("DELETE FROM environment WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM environment")
    suspend fun clearAll()
}
