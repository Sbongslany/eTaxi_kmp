package za.co.etaxi.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HttpClient factory + response unwrapping helpers.
 *
 * Built against Ktor 3.0.x (required by Kotlin 2.1.0).
 *
 * Base URL lives on every request via defaultRequest — callers use
 * relative paths like "auth/register", "drivers/me".
 *
 * Auth: we deliberately don't use the Auth plugin. Instead, call
 * `authorized(tokenManager)` inside each protected request builder
 * to attach the Bearer token. This keeps the refresh logic visible in
 * the repository layer rather than hidden in Ktor's plugin config.
 */
object EtaxiApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = false
        // If the backend sends null for a field that has a non-null
        // default, coerce to the default instead of throwing. This
        // prevents trip-request response parsing from blowing up on
        // optional fields that the create endpoint omits.
        coerceInputValues = true
    }

    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
            requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    println("[EtaxiApi] $message")
                }
            }
        }
        defaultRequest {
            // Ktor 3 DSL: url { takeFrom(...) } is the safe way to set
            // a base URL that callers extend with relative paths.
            url(ApiConfig.BASE_URL + "/")
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, "application/json")
        }
        expectSuccess = false  // we map status codes ourselves
    }

    /**
     * Wraps an HTTP call and returns a typed ApiResult.
     *
     * Handles:
     *   - The backend's { success, data, error } envelope
     *   - 401 with "phone_verification_required:<phone>" → PhoneVerificationRequired
     *   - Common HTTP error buckets (400, 401, 404, 409, 5xx)
     */
    suspend inline fun <reified T> wrap(
        crossinline block: suspend () -> HttpResponse,
    ): ApiResult<T> {
        return try {
            val res = block()
            when (res.status) {
                HttpStatusCode.OK,
                HttpStatusCode.Created,
                HttpStatusCode.Accepted -> {
                    val envelope: ApiEnvelope<T> = res.body()
                    val data = envelope.data
                    if (data != null) ApiResult.Success(data)
                    else ApiResult.Failure.Unknown("Empty response body")
                }
                HttpStatusCode.NoContent -> {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Success(Unit as T)
                }
                HttpStatusCode.Unauthorized -> {
                    val body = runCatching { res.body<ApiEnvelope<Unit>>() }.getOrNull()
                    val msg = body?.error?.message ?: "Unauthorized"
                    if (msg.startsWith("phone_verification_required:")) {
                        val phone = msg.substringAfter(":").trim()
                        ApiResult.Failure.PhoneVerificationRequired(phone)
                    } else {
                        ApiResult.Failure.Unauthorized(msg)
                    }
                }
                HttpStatusCode.NotFound -> {
                    val body = runCatching { res.body<ApiEnvelope<Unit>>() }.getOrNull()
                    ApiResult.Failure.NotFound(body?.error?.message ?: "Not found")
                }
                HttpStatusCode.Conflict -> {
                    val body = runCatching { res.body<ApiEnvelope<Unit>>() }.getOrNull()
                    ApiResult.Failure.Conflict(body?.error?.message ?: "Already exists")
                }
                HttpStatusCode.BadRequest,
                HttpStatusCode.UnprocessableEntity -> {
                    val body = runCatching { res.body<ApiEnvelope<Unit>>() }.getOrNull()
                    ApiResult.Failure.BadRequest(body?.error?.message ?: "Invalid request")
                }
                else -> {
                    if (res.status.value >= 500) {
                        ApiResult.Failure.Server("Server error (${res.status.value})")
                    } else {
                        ApiResult.Failure.Unknown("Unexpected status ${res.status.value}")
                    }
                }
            }
        } catch (e: Throwable) {
            ApiResult.Failure.Network(e.message ?: "Network error")
        }
    }
}

/**
 * Attach the current access token as Bearer on a request.
 * Idempotent — if no token, no header is set.
 */
fun HttpRequestBuilder.authorized(tokenManager: TokenManager) {
    tokenManager.getAccessToken()?.let {
        header(HttpHeaders.Authorization, "Bearer $it")
    }
}
