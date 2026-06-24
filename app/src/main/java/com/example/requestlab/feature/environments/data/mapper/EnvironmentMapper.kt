package com.example.requestlab.feature.environments.data.mapper

import com.example.requestlab.core.crypto.SecretCipher
import com.example.requestlab.feature.environments.data.entity.EnvironmentVariableEntity
import com.example.requestlab.feature.environments.data.entity.EnvironmentWithCount
import com.example.requestlab.feature.environments.domain.model.Environment
import com.example.requestlab.feature.environments.domain.model.Variable

fun EnvironmentWithCount.toDomain(activeId: String?): Environment = Environment(
    id = id,
    name = name,
    isActive = id == activeId,
    variableCount = variableCount,
)

fun EnvironmentVariableEntity.toDomain(cipher: SecretCipher): Variable = Variable(
    key = key,
    value = cipher.decrypt(valueCipher),
)

fun Variable.toEntity(environmentId: String, cipher: SecretCipher): EnvironmentVariableEntity =
    EnvironmentVariableEntity(
        environmentId = environmentId,
        key = key,
        valueCipher = cipher.encrypt(value),
    )
