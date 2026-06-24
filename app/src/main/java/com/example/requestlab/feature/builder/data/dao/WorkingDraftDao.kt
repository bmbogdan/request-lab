package com.example.requestlab.feature.builder.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.requestlab.feature.builder.data.entity.WorkingDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingDraftDao {
    @Query("SELECT * FROM working_draft WHERE id = 'current'")
    fun observe(): Flow<WorkingDraftEntity?>

    @Query("SELECT * FROM working_draft WHERE id = 'current'")
    suspend fun get(): WorkingDraftEntity?

    @Upsert
    suspend fun upsert(draft: WorkingDraftEntity)

    @Query("DELETE FROM working_draft")
    suspend fun clear()
}
