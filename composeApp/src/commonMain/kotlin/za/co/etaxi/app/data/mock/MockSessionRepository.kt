package za.co.etaxi.app.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.etaxi.app.domain.model.User
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository

class MockSessionRepository : SessionRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser = _currentUser.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    override val selectedRole = _selectedRole.asStateFlow()

    override suspend fun pickRole(role: UserRole) {
        _selectedRole.value = role
    }

    override suspend fun login(identifier: String, password: String): Result<User> {
        // Pretend-credentials: any email/phone + password >= 4 chars works.
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password too short"))
        }
        val role = _selectedRole.value ?: UserRole.RIDER
        val user = when (role) {
            UserRole.RIDER -> MockData.riderUser
            UserRole.DRIVER -> MockData.driverUser
        }
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        email: String,
        phone: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole,
    ): Result<User> {
        val user = when (role) {
            UserRole.RIDER  -> MockData.riderUser.copy(firstName = firstName, lastName = lastName, email = email, phone = phone)
            UserRole.DRIVER -> MockData.driverUser.copy(firstName = firstName, lastName = lastName, email = email, phone = phone)
        }
        _selectedRole.value = role
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun verifyOtp(code: String): Result<Unit> {
        // Accept any 4–6 digit numeric code.
        return if (code.length in 4..6 && code.all { it.isDigit() }) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Invalid OTP"))
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
        // Keep role so the role-switch UX still works.
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): Result<User> {
        val current = _currentUser.value
            ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
        )
        _currentUser.value = updated
        return Result.success(updated)
    }

    override suspend fun changePassword(current: String, next: String): Result<Unit> {
        if (current.length < 4) {
            return Result.failure(IllegalArgumentException("Current password is incorrect"))
        }
        if (next.length < 8) {
            return Result.failure(IllegalArgumentException("New password must be at least 8 characters"))
        }
        return Result.success(Unit)
    }
}
