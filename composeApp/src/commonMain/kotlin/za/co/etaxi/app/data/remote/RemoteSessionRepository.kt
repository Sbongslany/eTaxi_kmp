package za.co.etaxi.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.AuthResponse
import za.co.etaxi.app.core.network.LoginRequest
import za.co.etaxi.app.core.network.LogoutRequest
import za.co.etaxi.app.core.network.RegisterPendingResponse
import za.co.etaxi.app.core.network.RegisterRequest
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.network.UserDto
import za.co.etaxi.app.core.network.VerifyOtpRequest
import za.co.etaxi.app.domain.model.User
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository

/**
 * SessionRepository backed by the eTaxi backend at
 * http://192.168.0.181:5001/api/v1.
 *
 * Pattern-matching style: `is ApiResult.Success<*>` with an explicit
 * cast of res.value to the declared return type of the api method that
 * produced the result. This is safe because each RemoteAuthApi method
 * declares its concrete ApiResult<T> at the call site.
 */
class RemoteSessionRepository(
    private val authApi: RemoteAuthApi,
    private val tokens: TokenManager,
) : SessionRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    override val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()

    private var pendingOtpPhone: String? = null

    override suspend fun pickRole(role: UserRole) {
        _selectedRole.value = role
    }

    override suspend fun register(
        email: String,
        phone: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole,
    ): Result<User> {
        val req = RegisterRequest(
            email = email.trim(),
            phone = phone.trim(),
            password = password,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            role = role.name,
        )
        val res = authApi.register(req)
        return when (res) {
            is ApiResult.Success<*> -> {
                val pending = res.value as RegisterPendingResponse
                pendingOtpPhone = pending.phone
                _selectedRole.value = role
                Result.success(
                    User(
                        id = "pending-$phone",
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phone = phone,
                        role = role,
                    )
                )
            }
            is ApiResult.Failure.Conflict ->
                Result.failure(IllegalStateException(res.message))
            is ApiResult.Failure ->
                Result.failure(Exception(res.message))
        }
    }

    override suspend fun verifyOtp(code: String): Result<Unit> {
        val phone = pendingOtpPhone
            ?: return Result.failure(
                IllegalStateException("No OTP in progress. Register or log in first.")
            )
        val res = authApi.verifyOtp(VerifyOtpRequest(phone = phone, code = code))
        return when (res) {
            is ApiResult.Success<*> -> {
                val auth = res.value as AuthResponse
                tokens.saveAccessToken(auth.accessToken)
                tokens.saveRefreshToken(auth.refreshToken)
                _currentUser.value = auth.user.toDomain()
                _selectedRole.value = _currentUser.value?.role
                pendingOtpPhone = null
                Result.success(Unit)
            }
            is ApiResult.Failure.BadRequest ->
                Result.failure(IllegalArgumentException(res.message))
            is ApiResult.Failure ->
                Result.failure(Exception(res.message))
        }
    }

    override suspend fun login(identifier: String, password: String): Result<User> {
        val req = LoginRequest(identifier = identifier.trim(), password = password)
        val res = authApi.login(req)
        return when (res) {
            is ApiResult.Success<*> -> {
                val auth = res.value as AuthResponse
                tokens.saveAccessToken(auth.accessToken)
                tokens.saveRefreshToken(auth.refreshToken)
                val user = auth.user.toDomain()
                _currentUser.value = user
                _selectedRole.value = user.role
                pendingOtpPhone = null
                Result.success(user)
            }
            is ApiResult.Failure.PhoneVerificationRequired -> {
                pendingOtpPhone = res.phone
                Result.failure(PhoneVerificationRequiredException(res.phone))
            }
            is ApiResult.Failure.Unauthorized ->
                Result.failure(IllegalArgumentException("Invalid credentials"))
            is ApiResult.Failure ->
                Result.failure(Exception(res.message))
        }
    }

    override suspend fun logout() {
        val refreshToken = tokens.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            runCatching { authApi.logout(LogoutRequest(refreshToken)) }
        }
        tokens.clear()
        _currentUser.value = null
        _selectedRole.value = null
        pendingOtpPhone = null
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): Result<User> {
        val existing = _currentUser.value
            ?: return Result.failure(IllegalStateException("Not signed in"))
        val updated = existing.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
        )
        _currentUser.value = updated
        return Result.success(updated)
    }

    override suspend fun changePassword(current: String, next: String): Result<Unit> =
        Result.success(Unit)

    suspend fun restoreSession(): Boolean {
        if (!tokens.hasSession()) return false
        val res = authApi.me()
        return when (res) {
            is ApiResult.Success<*> -> {
                val userDto = res.value as UserDto
                val user = userDto.toDomain()
                _currentUser.value = user
                _selectedRole.value = user.role
                true
            }
            is ApiResult.Failure.Unauthorized -> {
                tokens.clear()
                false
            }
            is ApiResult.Failure -> false
        }
    }

    private fun UserDto.toDomain(): User = User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        role = when (role) {
            "DRIVER" -> UserRole.DRIVER
            else -> UserRole.RIDER
        },
        rating = riderProfile?.rating ?: driverProfile?.rating ?: 5.0,
        totalTrips = riderProfile?.totalTrips ?: driverProfile?.totalTrips ?: 0,
        profilePhotoUrl = profilePhoto,
    )
}

/**
 * Thrown by login() when the backend requires phone verification.
 * UI catches and navigates to OTP — the backend has auto-sent a code.
 */
class PhoneVerificationRequiredException(val phone: String) :
    Exception("Phone verification required for $phone")
