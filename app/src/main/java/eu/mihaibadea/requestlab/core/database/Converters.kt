package eu.mihaibadea.requestlab.core.database

import androidx.room.TypeConverter
import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.HttpMethod
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.model.RequestBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class Converters {

    @TypeConverter fun fromHttpMethod(method: HttpMethod): String = method.name
    @TypeConverter fun toHttpMethod(value: String): HttpMethod = HttpMethod.valueOf(value)

    @TypeConverter fun fromKeyValueList(list: List<KeyValue>): String = json.encodeToString(list)
    @TypeConverter fun toKeyValueList(value: String): List<KeyValue> = json.decodeFromString(value)

    @TypeConverter fun fromRequestBody(body: RequestBody): String = json.encodeToString(body)
    @TypeConverter fun toRequestBody(value: String): RequestBody = json.decodeFromString(value)

    @TypeConverter fun fromInstant(instant: java.time.Instant): Long = instant.toEpochMilli()
    @TypeConverter fun toInstant(value: Long): java.time.Instant = java.time.Instant.ofEpochMilli(value)
}
