package za.co.etaxi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.AuthResponse
import za.co.etaxi.app.core.network.EtaxiApi
import za.co.etaxi.app.core.network.LoginRequest
import za.co.etaxi.app.core.network.LogoutRequest
import za.co.etaxi.app.core.network.RefreshRequest
import za.co.etaxi.app.core.network.RegisterPendingResponse
import za.co.etaxi.app.core.network.RegisterRequest
import za.co.etaxi.app.core.network.SendOtpRequest
import za.co.etaxi.app.core.network.SendOtpResponse
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.network.UserDto
import za.co.etaxi.app.core.network.VerifyOtpRequest
import za.co.etaxi.app.core.network.authorized

// Raw HTTP client for the auth endpoints. No business logic here — just
// suspend wrappers around backend routes. Each method returns
// ApiResult so callers can pattern-match on Success/Failure without
// handling Ktor types directly.
//
// Endpoints (etaxi-backend Patch E):
//   POST auth/register            -> RegisterPendingResponse (NOT AuthResponse)
//   POST auth/phone/send-otp      -> SendOtpResponse
//   POST auth/phone/verify-otp    -> AuthResponse (tokens + user)
//   POST auth/login               -> AuthResponse or 401 phone_verification_required
//   POST auth/refresh             -> AuthResponse
//   POST auth/logout              -> Unit
//   GET  auth/me                  -> UserDto (requires Bearer token)
class RemoteAuthApi(
    private val http: HttpClient,
    private val tokens: TokenManager,
) {
    suspend fun register(req: RegisterRequest): ApiResult<RegisterPendingResponse> =
        EtaxiApi.wrap {
            http.post("auth/register") { setBody(req) }
        }

    suspend fun sendOtp(req: SendOtpRequest): ApiResult<SendOtpResponse> =
        EtaxiApi.wrap {
            http.post("auth/phone/send-otp") { setBody(req) }
        }

    suspend fun verifyOtp(req: VerifyOtpRequest): ApiResult<AuthResponse> =
        EtaxiApi.wrap {
            http.post("auth/phone/verify-otp") { setBody(req) }
        }

    suspend fun login(req: LoginRequest): ApiResult<AuthResponse> =
        EtaxiApi.wrap {
            http.post("auth/login") { setBody(req) }
        }

    suspend fun refresh(req: RefreshRequest): ApiResult<AuthResponse> =
        EtaxiApi.wrap {
            http.post("auth/refresh") { setBody(req) }
        }

    suspend fun logout(req: LogoutRequest): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("auth/logout") { setBody(req) }
        }

    suspend fun me(): ApiResult<UserDto> =
        EtaxiApi.wrap {
            http.get("auth/me") { authorized(tokens) }
        }
}
