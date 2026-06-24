package com.example.requestlab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.requestlab.feature.builder.data.dao.WorkingDraftDao
import com.example.requestlab.feature.builder.data.entity.WorkingDraftEntity
import com.example.requestlab.feature.collections.data.dao.CollectionDao
import com.example.requestlab.feature.collections.data.dao.SavedRequestDao
import com.example.requestlab.feature.collections.data.entity.CollectionEntity
import com.example.requestlab.feature.collections.data.entity.SavedRequestEntity
import com.example.requestlab.feature.environments.data.dao.EnvironmentDao
import com.example.requestlab.feature.environments.data.dao.EnvironmentVariableDao
import com.example.requestlab.feature.environments.data.entity.EnvironmentEntity
import com.example.requestlab.feature.environments.data.entity.EnvironmentVariableEntity
import com.example.requestlab.feature.history.data.dao.HistoryDao
import com.example.requestlab.feature.history.data.entity.HistoryEntity

@Database(
    entities = [
        WorkingDraftEntity::class,
        EnvironmentEntity::class,
        EnvironmentVariableEntity::class,
        HistoryEntity::class,
        CollectionEntity::class,
        SavedRequestEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class RequestLabDatabase : RoomDatabase() {
    abstract fun workingDraftDao(): WorkingDraftDao
    abstract fun environmentDao(): EnvironmentDao
    abstract fun environmentVariableDao(): EnvironmentVariableDao
    abstract fun historyDao(): HistoryDao
    abstract fun collectionDao(): CollectionDao
    abstract fun savedRequestDao(): SavedRequestDao
}
