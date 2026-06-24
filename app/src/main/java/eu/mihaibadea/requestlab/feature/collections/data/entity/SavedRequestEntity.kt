package eu.mihaibadea.requestlab.feature.collections.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_requests",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("collectionId")],
)
data class SavedRequestEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val name: String,
    val method: String,
    val url: String,
    val position: Int,
    val headersJson: String,
    val paramsJson: String,
    val bodyJson: String,
    val authJson: String,
)
