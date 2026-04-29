package za.co.etaxi.app.core.ws

// Typed events emitted by WsClient. Repositories subscribe to a Flow of these
// and translate them into changes in their own state flows (activeTrip,
// driverLocation, incomingRequests).
//
// The WS protocol on the server side is documented in
// src/config/constants.ts under WS_EVENT.
sealed interface TripEvent {
    val tripId: String

    // Sent to the driver when the matcher dispatches a new trip request.
    data class NewRideRequest(
        override val tripId: String,
        val pickupLat: Double,
        val pickupLng: Double,
        val pickupAddress: String,
        val dropoffLat: Double,
        val dropoffLng: Double,
        val dropoffAddress: String,
        val estimatedFareCents: Int,
        val currency: String,
        val pickupDistanceKm: Double,
    ) : TripEvent

    // Sent to either party when the trip's status field changes
    // (ACCEPTED, DRIVER_ARRIVING, ARRIVED, IN_PROGRESS, COMPLETED).
    data class StatusUpdate(
        override val tripId: String,
        val status: String,
        val driverId: String? = null,
        val finalFareCents: Int? = null,
    ) : TripEvent

    // Sent to the rider during IN_PROGRESS so they can see the driver
    // moving on the map.
    data class DriverLocation(
        override val tripId: String,
        val lat: Double,
        val lng: Double,
        val heading: Double? = null,
    ) : TripEvent

    // Sent to the other party when one side cancels.
    data class Cancelled(
        override val tripId: String,
        val by: String? = null,
        val reason: String? = null,
    ) : TripEvent
}
