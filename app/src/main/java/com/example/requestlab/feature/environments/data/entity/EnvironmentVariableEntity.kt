package com.example.requestlab.feature.environments.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "environment_variable",
    primaryKeys = ["environmentId", "key"],
    foreignKeys = [
        ForeignKey(
            entity = EnvironmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["environmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("environmentId")],
)
data class EnvironmentVariableEntity(
    val environmentId: String,
    val key: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val valueCipher: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EnvironmentVariableEntity) return false
        return environmentId == other.environmentId &&
            key == other.key &&
            valueCipher.contentEquals(other.valueCipher)
    }

    override fun hashCode(): Int {
        var result = environmentId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + valueCipher.contentHashCode()
        return result
    }
}
