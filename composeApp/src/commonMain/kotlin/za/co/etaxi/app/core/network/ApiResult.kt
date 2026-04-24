package za.co.etaxi.app.core.network

/**
 * Typed envelope for HTTP call results.
 *
 * Pattern-match style at call sites:
 *
 *   when (val res = api.login(req)) {
 *       is ApiResult.Success<*> -> {
 *           val auth = res.value as AuthResponse   // explicit cast needed
 *           ...
 *       }
 *       is ApiResult.Failure.Unauthorized -> ...
 *       is ApiResult.Failure -> ...
 *   }
 *
 * The `as` cast is safe because the outer ApiResult type is fixed at
 * the call site — `api.login()` declares `ApiResult<AuthResponse>`,
 * so Success.value must be AuthResponse in that branch.
 *
 * Alternative: use fold() / getOrNull() + onSuccess helpers to avoid
 * the cast entirely. These helpers ARE type-safe via smart-cast.
 */
sealed class ApiResult<out T> {

    class Success<out T>(val value: T) : ApiResult<T>() {
        override fun toString() = "Success($value)"
    }

    sealed class Failure : ApiResult<Nothing>() {
        abstract val message: String

        data class Network(override val message: String) : Failure()
        data class Unauthorized(override val message: String) : Failure()
        data class PhoneVerificationRequired(val phone: String) : Failure() {
            override val message = "Phone verification required"
        }
        data class Conflict(override val message: String) : Failure()
        data class NotFound(override val message: String) : Failure()
        data class BadRequest(override val message: String) : Failure()
        data class Server(override val message: String) : Failure()
        data class Unknown(override val message: String) : Failure()
    }

    @Suppress("UNCHECKED_CAST")
    fun getOrNull(): T? = (this as? Success<*>)?.value as? T

    @Suppress("UNCHECKED_CAST")
    inline fun onSuccess(block: (T) -> Unit): ApiResult<T> {
        if (this is Success<*>) block(value as T)
        return this
    }

    inline fun onFailure(block: (Failure) -> Unit): ApiResult<T> {
        if (this is Failure) block(this)
        return this
    }
}
