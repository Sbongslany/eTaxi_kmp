package za.co.etaxi.app.domain.model

import kotlinx.datetime.Instant

data class LatLng(val lat: Double, val lng: Double)

data class Place(
    val label: String,      // e.g. "Home"
    val address: String,    // e.g. "12 Helen Rd, Sandton"
    val location: LatLng,
)

enum class VehicleClass(val label: String, val maxSeats: Int, val eta: String) {
    STANDARD("Standard", 4, "3 min"),
    XL("XL", 6, "5 min"),
    LUX("Lux", 4, "7 min"),
}

data class VehicleClassOption(
    val vehicleClass: VehicleClass,
    val priceCents: Int,
    val currency: String = "ZAR",
)

enum class TripStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    DRIVER_ARRIVING,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    SCHEDULED,
}

data class DriverPreview(
    val id: String,
    val name: String,
    val rating: Double,
    val vehicleModel: String,
    val plateNumber: String,
    val photoUrl: String? = null,
)

data class Trip(
    val id: String,
    val status: TripStatus,
    val pickup: Place,
    val dropoff: Place,
    val stops: List<Place> = emptyList(),
    val vehicleClass: VehicleClass = VehicleClass.STANDARD,
    val fareCents: Int,
    val distanceKm: Double,
    val durationMinutes: Int,
    val requestedAt: Instant,
    val driver: DriverPreview? = null,
)

data class DriverIncomingRequest(
    val tripId: String,
    val riderName: String,
    val riderRating: Double,
    val pickup: Place,
    val dropoff: Place,
    val distanceKm: Double,
    val durationMinutes: Int,
    val fareCents: Int,
    val pickupDistanceKm: Double,  // distance driver has to drive to get to pickup
    val expiresAtSeconds: Int = 15,
)
