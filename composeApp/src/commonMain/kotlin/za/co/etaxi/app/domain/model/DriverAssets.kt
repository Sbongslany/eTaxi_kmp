package za.co.etaxi.app.domain.model

import kotlinx.datetime.Instant

// =========================================
// Vehicles (driver-owned)
// =========================================

enum class VehicleStatus { PENDING, APPROVED, REJECTED, DEACTIVATED }

data class DriverVehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plateNumber: String,
    val vehicleClass: VehicleClass,
    val status: VehicleStatus,
    val isActive: Boolean = false,
) {
    val displayName: String get() = "$year $make $model"
}

// =========================================
// Documents
// =========================================

enum class DocumentType(val label: String, val required: Boolean = true) {
    ID_DOCUMENT         ("ID Document"),
    DRIVERS_LICENSE     ("Driver's License"),
    PRDP                ("PrDP"),
    VEHICLE_REGISTRATION("Vehicle Registration"),
    ROADWORTHY          ("Roadworthy Certificate"),
    OPERATING_LICENSE   ("Operating License"),
    INSURANCE           ("Insurance"),
    PROFILE_PHOTO       ("Profile Photo", required = false),
}

enum class DocumentStatus { MISSING, PENDING, APPROVED, REJECTED, EXPIRED }

data class DriverDocument(
    val type: DocumentType,
    val status: DocumentStatus,
    val uploadedAt: Instant? = null,
    val expiresAt: Instant? = null,
    val rejectionReason: String? = null,
)

// =========================================
// Payment methods (rider)
// =========================================

enum class CardBrand(val label: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("Amex"),
    OTHER("Card"),
}

data class PaymentMethod(
    val id: String,
    val brand: CardBrand,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean = false,
)

// =========================================
// Wallet (driver)
// =========================================

enum class WalletTxType { CREDIT, DEBIT }
enum class WalletTxReason { TRIP_EARNING, PAYOUT, ADJUSTMENT, BONUS }

data class WalletTransaction(
    val id: String,
    val amountCents: Long,
    val type: WalletTxType,
    val reason: WalletTxReason,
    val description: String,
    val createdAt: Instant,
)

// =========================================
// Ratings
// =========================================

data class RatingSubmission(
    val tripId: String,
    val stars: Int,
    val comment: String? = null,
)
