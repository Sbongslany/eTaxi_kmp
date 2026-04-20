package za.co.etaxi.app.domain.model

enum class UserRole { RIDER, DRIVER }

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val rating: Double = 5.0,
    val totalTrips: Int = 0,
    val profilePhotoUrl: String? = null,
) {
    val displayName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: '?'}${lastName.firstOrNull() ?: '?'}".uppercase()
}
