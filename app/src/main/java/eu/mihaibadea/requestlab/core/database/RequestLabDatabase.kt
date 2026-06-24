package eu.mihaibadea.requestlab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.mihaibadea.requestlab.feature.builder.data.dao.WorkingDraftDao
import eu.mihaibadea.requestlab.feature.builder.data.entity.WorkingDraftEntity
import eu.mihaibadea.requestlab.feature.collections.data.dao.CollectionDao
import eu.mihaibadea.requestlab.feature.collections.data.dao.SavedRequestDao
import eu.mihaibadea.requestlab.feature.collections.data.entity.CollectionEntity
import eu.mihaibadea.requestlab.feature.collections.data.entity.SavedRequestEntity
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentDao
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentVariableDao
import eu.mihaibadea.requestlab.feature.environments.data.entity.EnvironmentEntity
import eu.mihaibadea.requestlab.feature.environments.data.entity.EnvironmentVariableEntity
import eu.mihaibadea.requestlab.feature.history.data.dao.HistoryDao
import eu.mihaibadea.requestlab.feature.history.data.entity.HistoryEntity

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
