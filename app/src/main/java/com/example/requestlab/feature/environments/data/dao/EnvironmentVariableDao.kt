package com.example.requestlab.feature.environments.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.requestlab.feature.environments.data.entity.EnvironmentVariableEntity

@Dao
interface EnvironmentVariableDao {

    @Query("SELECT * FROM environment_variable WHERE environmentId = :id")
    suspend fun getForEnvironment(id: String): List<EnvironmentVariableEntity>

    @Query("DELETE FROM environment_variable WHERE environmentId = :id")
    suspend fun deleteForEnvironment(id: String)

    @Upsert
    suspend fun upsertAll(vars: List<EnvironmentVariableEntity>)

    @Transaction
    suspend fun replaceAll(id: String, vars: List<EnvironmentVariableEntity>) {
        deleteForEnvironment(id)
        upsertAll(vars)
    }
}
