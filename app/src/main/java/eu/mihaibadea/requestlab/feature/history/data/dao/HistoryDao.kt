package eu.mihaibadea.requestlab.feature.history.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.mihaibadea.requestlab.feature.history.data.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY sentAtMillis DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getById(id: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}
