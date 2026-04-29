package za.co.etaxi.app.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import za.co.etaxi.app.core.network.AddStopBody
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.RatingBody
import za.co.etaxi.app.core.network.RequestStandardTripBody
import za.co.etaxi.app.core.network.TripDto
import za.co.etaxi.app.core.network.TripPointDto
import za.co.etaxi.app.core.ws.TripEvent
import za.co.etaxi.app.core.ws.WsClient
import za.co.etaxi.app.domain.model.DriverPreview
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleClassOption
import za.co.etaxi.app.domain.repository.RiderRepository
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RemoteRiderRepository(
    private val tripApi: RemoteTripApi,
    private val ws: WsClient,
) : RiderRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Currently active trip (if any). Updated by both:
    //   1. The HTTP response when the rider requests a trip
    //   2. WebSocket TRIP_STATUS_UPDATE / TRIP_CANCELLED events
    private val _activeTrip = MutableStateFlow<Trip?>(null)

    // Live driver location during IN_PROGRESS / DRIVER_ARRIVING.
    // Driven exclusively by WS DRIVER_LOCATION_UPDATE events.
    private val _driverLocation = MutableStateFlow<LatLng?>(null)

    init {
        // Start (or join) the persistent WS connection. connect() is
        // idempotent — if the driver repo or another consumer already
        // started it, this is a no-op.
        ws.connect()
        // Start consuming WS events. The flow is hot — events are emitted
        // whenever WsClient receives them. When the active trip's status
        // changes we re-fetch the trip details (because some events
        // don't carry every field, e.g. status without driver info).
        scope.launch {
            ws.events.collect { event -> onWsEvent(event) }
        }
    }

    override fun activeTrip(): Flow<Trip?> = _activeTrip.asStateFlow()
    override fun driverLocation(): Flow<LatLng?> = _driverLocation.asStateFlow()

    // ============== Saved places & estimate ==============
    // The backend doesn't yet have endpoints for saved places or
    // a pricing-only estimate — both are computed client-side from
    // simple haversine distance and the static tariff table on
    // VehicleClass. When those backend endpoints land we move these
    // methods to call them instead.
    override suspend fun savedPlaces(): List<Place> = listOf(
        Place(
            label = "Home",
            address = "12 Helen Rd, Sandton",
            location = LatLng(-26.1076, 28.0567),
        ),
        Place(
            label = "Work",
            address = "1 Discovery Place, Sandton",
            location = LatLng(-26.1067, 28.0560),
        ),
    )

    override suspend fun estimate(pickup: Place, dropoff: Place): List<VehicleClassOption> {
        val km = haversineKm(pickup.location, dropoff.location)
        return VehicleClass.entries.map { cls ->
            // Quick local estimate. Real pricing comes back from
            // /trips/request response.
            val perKm = when (cls) {
                VehicleClass.STANDARD -> 1300
                VehicleClass.XL       -> 1900
                VehicleClass.LUX      -> 2800
            }
            val base = when (cls) {
                VehicleClass.STANDARD -> 2500
                VehicleClass.XL       -> 3500
                VehicleClass.LUX      -> 5500
            }
            VehicleClassOption(
                vehicleClass = cls,
                priceCents = (base + (km * perKm)).toInt(),
                currency = "ZAR",
            )
        }
    }

    // ============== Request / cancel / rate / addStop ==============
    override suspend fun requestTrip(
        pickup: Place,
        dropoff: Place,
        option: VehicleClassOption,
    ): Trip {
        val vehicleTypeId = vehicleClassToTypeCode(option.vehicleClass)
        val res = tripApi.requestStandard(
            RequestStandardTripBody(
                vehicleTypeId = vehicleTypeId,
                pickup = TripPointDto(pickup.location.lat, pickup.location.lng, pickup.address),
                dropoff = TripPointDto(dropoff.location.lat, dropoff.location.lng, dropoff.address),
            )
        )
        return if (res is ApiResult.Success<*>) {
            val response = res.value as za.co.etaxi.app.core.network.RequestTripResponse
            val trip = response.trip.toDomain(pickup, dropoff)
            _activeTrip.value = trip
            trip
        } else {
            val failure = res as ApiResult.Failure
            throw Exception("Trip request failed: ${failure.message}")
        }
    }

    override suspend fun history(): List<Trip> {
        // Backend's GET /trips returns paginated history. For now we just
        // call it and map the items. Pagination wiring deferred until UI
        // needs it.
        return emptyList()
    }

    override suspend fun getTrip(id: String): Trip? {
        val res = tripApi.get(id)
        return if (res is ApiResult.Success<*>) {
            (res.value as TripDto).toDomain()
        } else null
    }

    override suspend fun cancelTrip(tripId: String) {
        val res = tripApi.cancel(tripId, reason = "Cancelled by rider")
        if (res is ApiResult.Success<*>) {
            _activeTrip.value = null
        }
    }

    override suspend fun addStop(tripId: String, stop: Place): Trip {
        val res = tripApi.addStop(
            tripId,
            AddStopBody(
                lat = stop.location.lat,
                lng = stop.location.lng,
                address = stop.address,
            )
        )
        return if (res is ApiResult.Success<*>) {
            // Backend's addStop returns { trip, stop, fare } but we
            // accept it as TripDto-shaped fallback. Re-fetch to be safe.
            refreshActive(tripId) ?: (res.value as TripDto).toDomain()
        } else {
            val failure = res as ApiResult.Failure
            throw Exception("Add stop failed: ${failure.message}")
        }
    }

    override suspend fun rateDriver(submission: RatingSubmission) {
        val active = _activeTrip.value
        val toUserId = active?.driver?.id ?: return
        tripApi.rate(
            RatingBody(
                tripId = submission.tripId,
                toUserId = toUserId,
                stars = submission.stars,
                comment = submission.comment,
            )
        )
        // After rating, the trip is fully done — clear active state.
        _activeTrip.value = null
    }

    // ================== WebSocket event handling ==================
    private suspend fun onWsEvent(event: TripEvent) {
        when (event) {
            is TripEvent.StatusUpdate -> {
                // Re-fetch full trip details because the event payload
                // alone doesn't include driver/vehicle info.
                refreshActive(event.tripId)
            }
            is TripEvent.DriverLocation -> {
                _driverLocation.value = LatLng(event.lat, event.lng)
            }
            is TripEvent.Cancelled -> {
                if (_activeTrip.value?.id == event.tripId) {
                    _activeTrip.value = null
                    _driverLocation.value = null
                }
            }
            is TripEvent.NewRideRequest -> {
                // Rider doesn't care about NEW_RIDE_REQUEST events.
            }
        }
    }

    private suspend fun refreshActive(tripId: String): Trip? {
        val res = tripApi.get(tripId)
        if (res !is ApiResult.Success<*>) return null
        val trip = (res.value as TripDto).toDomain()
        // If the trip is already terminal (completed/cancelled), keep
        // the rider on the rating/fare screen by leaving _activeTrip set
        // until they explicitly rate or dismiss.
        _activeTrip.value = trip
        return trip
    }

    // ================== Mappers ==================
    private fun TripDto.toDomain(
        pickupOverride: Place? = null,
        dropoffOverride: Place? = null,
    ): Trip {
        val pickup = pickupOverride ?: Place(
            label = "Pickup",
            address = pickupAddress,
            location = LatLng(pickupLat, pickupLng),
        )
        val dropoff = dropoffOverride ?: Place(
            label = "Dropoff",
            address = dropoffAddress,
            location = LatLng(dropoffLat, dropoffLng),
        )
        val cls = vehicleTypeIdToClass(vehicleTypeId)
        val parsedRequestedAt = createdAt
            ?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?: Clock.System.now()
        val driverPreview = driver?.let { d ->
            val name = listOfNotNull(d.user?.firstName, d.user?.lastName)
                .joinToString(" ").ifBlank { "Driver" }
            DriverPreview(
                id = d.user?.id ?: d.id,
                name = name,
                rating = d.rating ?: 5.0,
                vehicleModel = d.activeVehicle
                    ?.let { "${it.make} ${it.model}" }
                    ?: vehicle?.let { "${it.make} ${it.model}" }
                    ?: "",
                plateNumber = d.activeVehicle?.plateNumber
                    ?: vehicle?.plateNumber
                    ?: "",
                photoUrl = d.user?.profilePhoto,
            )
        }
        return Trip(
            id = id,
            status = parseStatus(status),
            pickup = pickup,
            dropoff = dropoff,
            stops = stops.map { Place(label = "Stop", address = it.address, location = LatLng(it.lat, it.lng)) },
            vehicleClass = cls,
            fareCents = finalFareCents ?: estimatedFareCents,
            distanceKm = (distanceMeters ?: 0) / 1000.0,
            durationMinutes = ((durationSeconds ?: 0) / 60),
            requestedAt = parsedRequestedAt,
            driver = driverPreview,
        )
    }

    private fun parseStatus(s: String): TripStatus =
        runCatching { TripStatus.valueOf(s) }.getOrDefault(TripStatus.REQUESTED)

    // The backend stores vehicleTypeId as a UUID. When we request a trip we
    // send the readable code (STANDARD/XL/LUX) and the backend resolves it.
    // When parsing a returned trip we get the UUID back, which we can't
    // map back to an enum without an extra lookup. Default to STANDARD.
    private fun vehicleTypeIdToClass(id: String): VehicleClass =
        runCatching { VehicleClass.valueOf(id.uppercase()) }
            .getOrDefault(VehicleClass.STANDARD)

    private fun vehicleClassToTypeCode(cls: VehicleClass): String = cls.name

    private fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0
        val dLat = toRad(b.lat - a.lat)
        val dLng = toRad(b.lng - a.lng)
        val sinDLat = sin(dLat / 2)
        val sinDLng = sin(dLng / 2)
        val h = sinDLat * sinDLat +
            cos(toRad(a.lat)) * cos(toRad(b.lat)) *
            sinDLng * sinDLng
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return r * c
    }

    private fun toRad(deg: Double): Double = deg * (3.141592653589793 / 180.0)
}
