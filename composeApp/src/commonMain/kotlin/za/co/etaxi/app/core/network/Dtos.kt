package za.co.etaxi.app.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs mirroring the eTaxi backend JSON contract.
 *
 * These are WIRE FORMAT types only — they exist to serialize/deserialize
 * HTTP payloads. They MUST stay in lockstep with the backend's zod
 * schemas and Prisma types. Do NOT use these in UI code directly; map
 * them to domain models (User, Vehicle, etc.) in the repository layer.
 *
 * All backend responses are wrapped by an envelope: { success, data }
 * for ok() responses or { success: false, error } for errors. We
 * unwrap in EtaxiApi so DTOs reflect the `data` payload only.
 */

// ─────────────────────────── Backend response envelope ───────────────────

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = true,
    val data: T? = null,
    val error: ApiErrorBody? = null,
)

@Serializable
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
    val details: String? = null,
)

// ─────────────────────────── Auth ────────────────────────────────────────

@Serializable
data class RegisterRequest(
    val email: String,
    val phone: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String, // "RIDER" | "DRIVER"
)

/**
 * Patch E of the backend changes /auth/register — it no longer returns
 * tokens, but rather { requiresOtp, phone, expiresAt }. We model that as
 * a sealed response the repository unwraps.
 */
@Serializable
data class RegisterPendingResponse(
    val requiresOtp: Boolean = true,
    val phone: String,
    val expiresAt: String,
)

@Serializable
data class LoginRequest(
    val identifier: String, // email or phone
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
data class SendOtpRequest(
    val phone: String,
    val purpose: String = "REGISTRATION", // REGISTRATION | LOGIN | PASSWORD_RESET
)

@Serializable
data class SendOtpResponse(
    val expiresAt: String,
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val code: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val role: String,            // "RIDER" | "DRIVER" | "ADMIN" | "SUPER_ADMIN"
    val status: String? = null,  // "ACTIVE" | "SUSPENDED" | "DELETED"
    val profilePhoto: String? = null,
    val emailVerified: Boolean? = null,
    val phoneVerified: Boolean? = null,
    val riderProfile: RiderProfileDto? = null,
    val driverProfile: DriverProfileDto? = null,
)

@Serializable
data class RiderProfileDto(
    val id: String,
    val homeAddress: String? = null,
    val workAddress: String? = null,
    val preferredPayment: String? = null,
    val rating: Double? = null,
    val totalTrips: Int? = null,
)

@Serializable
data class DriverProfileDto(
    val id: String,
    val status: String,            // PENDING | VERIFIED | SUSPENDED | DEACTIVATED
    val availability: String,      // OFFLINE | ONLINE | ON_TRIP
    val rating: Double? = null,
    val totalTrips: Int? = null,
    val idNumber: String? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val activeVehicle: VehicleDto? = null,
)

// ─────────────────────────── Vehicles ────────────────────────────────────

@Serializable
data class VehicleDto(
    val id: String,
    val vehicleTypeId: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plateNumber: String,
    val vinNumber: String? = null,
    val status: String,           // PENDING | APPROVED | REJECTED | DEACTIVATED
    val photoUrl: String? = null,
)

@Serializable
data class AddVehicleRequest(
    val vehicleTypeId: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plateNumber: String,
    val vinNumber: String? = null,
    val photoUrl: String? = null,
)

@Serializable
data class SetActiveVehicleRequest(
    val vehicleId: String,
)

// ─────────────────────────── Documents ───────────────────────────────────

@Serializable
data class DocumentDto(
    val id: String,
    val type: String,
    val status: String,
    val url: String,
    val issuedAt: String? = null,
    val expiresAt: String? = null,
    val reviewedAt: String? = null,
    val rejectionReason: String? = null,
)

@Serializable
data class UploadDocumentRequest(
    val type: String,
    val url: String,
    val issuedAt: String? = null,
    val expiresAt: String? = null,
)

// ─────────────────────────── Driver actions ──────────────────────────────

@Serializable
data class GoOnlineRequest(
    val lat: Double,
    val lng: Double,
)

@Serializable
data class UpdateLocationRequest(
    val lat: Double,
    val lng: Double,
    val heading: Double? = null,
    val speed: Double? = null,
)

// ─────────────────────────── Storage (presigned upload) ──────────────────

@Serializable
data class UploadUrlRequest(
    val folder: String, // "documents" | "vehicles" | "avatars"
    val contentType: String, // "image/jpeg" | "image/png" | "image/webp" | "application/pdf"
    val filename: String? = null,
)

@Serializable
data class UploadUrlResponse(
    val uploadUrl: String,
    @SerialName("publicUrl") val publicUrl: String? = null,
    val key: String? = null,
    val expiresInSeconds: Int? = null,
)
