package com.example.requestlab.core.common

import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

suspend fun <T> runCatchingToAppResult(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e  // never swallow coroutine cancellation
    } catch (e: SocketTimeoutException) {
        AppResult.Failure(AppError.Timeout(seconds = 0))
    } catch (e: UnknownHostException) {
        AppResult.Failure(AppError.Dns)
    } catch (e: SSLException) {
        AppResult.Failure(AppError.Tls)
    } catch (e: android.database.sqlite.SQLiteException) {
        AppResult.Failure(AppError.Storage(e))
    } catch (e: java.io.IOException) {
        AppResult.Failure(AppError.Storage(e))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e))
    }
}
