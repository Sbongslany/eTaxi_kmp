package za.co.etaxi.app.core.network

import kotlinx.serialization.Serializable

// Trip lifecycle wire format. Mirrors backend zod schemas in
// src/modules/trip/application/dto/trip.dto.ts and the Trip Prisma model.

// Common pickup/dropoff coordinate pair
@Serializable
data class TripPointDto(
    val lat: Double,
    val lng: Double,
    val address: String,
)

// POST /trips/request body
@Serializable
data class RequestStandardTripBody(
    val vehicleTypeId: String,
    val pickup: TripPointDto,
    val dropoff: TripPointDto,
)

// POST /trips/:id/cancel body
@Serializable
data class CancelTripBody(
    val reason: String? = null,
)

// POST /trips/:id/stops body
@Serializable
data class AddStopBody(
    val lat: Double,
    val lng: Double,
    val address: String,
)

// PATCH /trips/:id/stops/reorder body
@Serializable
data class ReorderStopsBody(
    val stopIds: List<String>,
)

// POST /trips/:id/location body (driver pings)
@Serializable
data class LocationPingBody(
    val lat: Double,
    val lng: Double,
    val heading: Double? = null,
    val speed: Double? = null,
)

// POST /ratings body (rate the other party after a trip)
@Serializable
data class RatingBody(
    val tripId: String,
    val toUserId: String,
    val stars: Int,
    val comment: String? = null,
)

// Backend response shape for a single Trip including nested driver/vehicle.
// Fields are optional or nullable where they may not exist before certain
// state transitions (e.g. driver before ACCEPTED).
@Serializable
data class TripDto(
    val id: String,
    val type: String,
    val status: String,
    val riderId: String,
    val driverId: String? = null,
    val vehicleId: String? = null,
    val vehicleTypeId: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val dropoffAddress: String,
    val estimatedFareCents: Int,
    val finalFareCents: Int? = null,
    val currency: String = "ZAR",
    val distanceMeters: Int? = null,
    val durationSeconds: Int? = null,
    val acceptedAt: String? = null,
    val arrivedAt: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val cancelledAt: String? = null,
    val createdAt: String? = null,
    val driver: TripDriverNestedDto? = null,
    val rider: TripRiderNestedDto? = null,
    val vehicle: TripVehicleNestedDto? = null,
    val stops: List<TripStopDto> = emptyList(),
)

// Rider info that the backend's getById() returns under `rider`.
@Serializable
data class TripRiderNestedDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePhoto: String? = null,
)

// Nested objects that the backend's getById() include returns.
// We don't need the full DriverProfile/Vehicle here — just the bits the
// rider's "driver assigned" panel and the in-trip header need.
@Serializable
data class TripDriverNestedDto(
    val id: String,
    val rating: Double? = null,
    val user: TripDriverUserDto? = null,
    val activeVehicle: TripVehicleNestedDto? = null,
)

@Serializable
data class TripDriverUserDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val profilePhoto: String? = null,
)

@Serializable
data class TripVehicleNestedDto(
    val id: String,
    val make: String,
    val model: String,
    val plateNumber: String,
    val color: String? = null,
)

@Serializable
data class TripStopDto(
    val id: String,
    val sequence: Int,
    val lat: Double,
    val lng: Double,
    val address: String,
)

// Backend's POST /trips/:id/stops returns { trip, stop, fare } not a
// bare TripDto. We define a wrapper so the deserializer matches the
// real wire format. Without this, the request "succeeds" on the
// network layer but parsing the response fails or yields a TripDto
// with all-default fields, which then crashes downstream consumers.
@Serializable
data class AddStopResponseDto(
    val trip: TripDto,
    val stop: TripStopDto? = null,
    val fare: TripEstimateDto? = null,
)

// Backend wraps single-trip responses in { trip, estimate } on request,
// but for accept/get/list etc. it returns the trip directly. To keep the
// API surface small we accept either shape via two response wrappers.
@Serializable
data class RequestTripResponse(
    val trip: TripDto,
    val estimate: TripEstimateDto? = null,
)

@Serializable
data class TripEstimateDto(
    val totalCents: Int,
    val currency: String = "ZAR",
    val distanceKm: Double? = null,
    val durationMinutes: Double? = null,
    val vatCents: Int? = null,
)

// =================== WebSocket events ====================
// The backend pushes events with shape:
//   { type: "<EVENT_NAME>", tripId: "...", payload: { ... } }
// We discriminate on `type` and route to typed event objects in WsClient.

@Serializable
data class WsEnvelope(
    val type: String,
    val tripId: String? = null,
    val payload: WsPayload? = null,
)

// All event payloads we care about, packed as optional fields. The WsClient
// inspects `type` and constructs a typed TripEvent from this.
@Serializable
data class WsPayload(
    val status: String? = null,
    val driverId: String? = null,
    val finalFareCents: Int? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val heading: Double? = null,
    val by: String? = null,
    val reason: String? = null,
    // For NEW_RIDE_REQUEST (driver inbox)
    val pickup: TripPointDto? = null,
    val dropoff: TripPointDto? = null,
    val estimatedFareCents: Int? = null,
    val currency: String? = null,
    val distanceKm: Double? = null,
)
