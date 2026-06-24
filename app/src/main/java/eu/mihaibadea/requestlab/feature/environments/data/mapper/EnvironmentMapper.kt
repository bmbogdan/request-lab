package eu.mihaibadea.requestlab.feature.environments.data.mapper

import eu.mihaibadea.requestlab.core.crypto.SecretCipher
import eu.mihaibadea.requestlab.feature.environments.data.entity.EnvironmentVariableEntity
import eu.mihaibadea.requestlab.feature.environments.data.entity.EnvironmentWithCount
import eu.mihaibadea.requestlab.feature.environments.domain.model.Environment
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable

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
