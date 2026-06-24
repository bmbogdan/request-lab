package eu.mihaibadea.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface RequestBody {
    @Serializable data object None : RequestBody
    @Serializable data class Json(val text: String) : RequestBody
    @Serializable data class RawText(val text: String, val contentType: String) : RequestBody
    @Serializable data class FormUrlEncoded(val fields: List<KeyValue>) : RequestBody
    @Serializable data class Multipart(val parts: List<MultipartPart>) : RequestBody
    @Serializable data class Binary(val file: FileRef) : RequestBody
}

@Serializable
sealed interface MultipartPart {
    @Serializable data class Text(val name: String, val value: String) : MultipartPart
    @Serializable data class FilePart(val name: String, val file: FileRef) : MultipartPart
}
