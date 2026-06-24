package eu.mihaibadea.requestlab.core.common

sealed interface AppError {
    data object NoInternet : AppError
    data class Timeout(val seconds: Int) : AppError
    data object Dns : AppError
    data object Tls : AppError
    data object Cancelled : AppError
    data class Validation(val field: String, val message: String) : AppError
    data object NotFound : AppError
    data class Storage(val cause: Throwable) : AppError
    data class Unknown(val cause: Throwable) : AppError
}
