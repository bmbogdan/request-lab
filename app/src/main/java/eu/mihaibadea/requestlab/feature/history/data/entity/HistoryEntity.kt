package eu.mihaibadea.requestlab.feature.history.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index("sentAtMillis")],
)
data class HistoryEntity(
    @PrimaryKey val id: String,
    val method: String,
    val resolvedUrl: String,
    val statusType: String,      // "HTTP" or "FAILED"
    val statusCode: Int?,
    val failureKind: String?,
    val failureReason: String?,
    val latencyMs: Long?,
    val sentAtMillis: Long,
    val environmentName: String?,
    val requestJson: String,
    val responseJson: String?,
)
