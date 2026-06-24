package com.example.requestlab.feature.builder.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "working_draft")
data class WorkingDraftEntity(
    @PrimaryKey val id: String,
    val method: String,
    val url: String,
    val headersJson: String,
    val paramsJson: String,
    val bodyJson: String,
    val authType: String,               // NONE | BASIC | BEARER
    val authUsername: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val authSecretCipher: ByteArray?,   // AES/GCM encrypted password or token
    val sourceSavedRequestId: String?,
    val isDirty: Boolean,
) {
    // ByteArray breaks structural equality — override so entity comparisons are correct
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorkingDraftEntity) return false
        return id == other.id && method == other.method && url == other.url &&
            headersJson == other.headersJson && paramsJson == other.paramsJson &&
            bodyJson == other.bodyJson && authType == other.authType &&
            authUsername == other.authUsername &&
            authSecretCipher.contentEquals(other.authSecretCipher) &&
            sourceSavedRequestId == other.sourceSavedRequestId && isDirty == other.isDirty
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + authType.hashCode()
        result = 31 * result + (authSecretCipher?.contentHashCode() ?: 0)
        return result
    }
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
    if (this == null && other == null) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}
