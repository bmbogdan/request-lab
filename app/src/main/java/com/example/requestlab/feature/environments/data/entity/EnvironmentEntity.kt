package com.example.requestlab.feature.environments.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "environment")
data class EnvironmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
)
