package com.nrvz.prebuildapplication.network

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * ============================================================================
 *  NetworkResult + safeApiCall — one wrapper for every network call.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * THE PROBLEM: every Retrofit `suspend fun` throws on failure. A 404 throws
 * HttpException, no internet throws IOException, and a bug in your repository
 * throws something else entirely. If you wrap only the Retrofit call in
 * try/catch, that third case goes straight to the user as a crash.
 *
 * THE FIX: one helper, used by EVERY repository method that talks to the server.
 * It converts any failure into [NetworkResult.Error] so the caller gets a value
 * to handle instead of a stack trace.
 *
 * USAGE (see NoteRepository.syncFromServer):
 *   when (val result = safeApiCall { apiService.getPosts() }) {
 *       is NetworkResult.Success -> ... result.data ...
 *       is NetworkResult.Error   -> ... result.message ...
 *   }
 *
 * RockyGo's equivalent is `safeApiCall` + `ResultWrapper` in its `grglib` module,
 * which additionally parses the server's own message out of the error body. The
 * `messageFromErrorBody` helper below is the trimmed-down version of that idea.
 */
sealed class NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>()

    data class Error(
        val code: Int? = null,
        val message: String,
    ) : NetworkResult<Nothing>()

    /** true when the failure is "the device has no usable network". */
    fun isNetworkError(): Boolean = this is Error && (code == null || code >= 500)
}

private const val TAG = "safeApiCall"

/**
 * Runs [block] and turns ANY throwable into a [NetworkResult].
 *
 * IMPORTANT: this always switches to Dispatchers.IO, so callers do not have to
 * remember to. (Retrofit's suspend functions already do this internally, but
 * repositories also do database work next to the network call in the same block.)
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> =
    withContext(Dispatchers.IO) {
        try {
            NetworkResult.Success(block())
        } catch (e: CancellationException) {
            // CRITICAL: never swallow cancellation. If the ViewModel was cleared
            // (user left the screen) this exception is the cancellation signal
            // travelling up the coroutine tree. Catching it here without
            // rethrowing breaks structured concurrency and leaks work.
            throw e
        } catch (e: HttpException) {
            // Server answered, but with 4xx / 5xx.
            NetworkResult.Error(e.code(), messageFromErrorBody(e))
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error(null, "The server took too long to answer.")
        } catch (e: IOException) {
            // No wifi/data, DNS failure, connection reset.
            NetworkResult.Error(null, "No internet connection. Check your network.")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected failure", e)
            NetworkResult.Error(null, e.message ?: "Something went wrong.")
        }
    }

/**
 * Best-effort "what did the server actually say?".
 * A surprising number of APIs return a helpful JSON message inside an error body
 * while HttpException.message() only gives you "HTTP 400 Bad Request".
 * Falls back to the exception text when the body is not JSON.
 */
private fun messageFromErrorBody(e: HttpException): String {
    val fallback = e.message() ?: "Request failed (${e.code()})"
    return try {
        val body = e.response()?.errorBody()?.string()
        if (body.isNullOrBlank()) fallback
        else JSONObject(body).optString("message").ifBlank { fallback }
    } catch (_: Exception) {
        fallback
    }
}
