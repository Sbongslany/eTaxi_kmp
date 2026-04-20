package za.co.etaxi.app.data.mock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.model.VehicleClassOption
import za.co.etaxi.app.domain.repository.RiderRepository

/**
 * Mock rider repo with an auto-advancing trip state machine.
 *
 * When a trip is requested, a background coroutine walks it through the
 * realistic lifecycle with ~5s between each status so the UI animates
 * naturally. A simulated driver-location flow drifts from pickup toward
 * dropoff during `IN_PROGRESS`.
 */
class MockRiderRepository : RiderRepository {

    private val _activeTrip = MutableStateFlow<Trip?>(null)
    override fun activeTrip() = _activeTrip.asStateFlow()

    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    override fun driverLocation() = _driverLocation.asStateFlow()

    // Background work
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var autoAdvanceJob: Job? = null
    private var driverTrailJob: Job? = null

    // Local mutable history so completed trips show up
    private val _history = MockData.riderHistory.toMutableList()

    override suspend fun savedPlaces(): List<Place> = MockData.savedPlaces

    override suspend fun estimate(pickup: Place, dropoff: Place): List<VehicleClassOption> {
        delay(400)
        return MockData.estimate(pickup, dropoff)
    }

    override suspend fun requestTrip(
        pickup: Place,
        dropoff: Place,
        option: VehicleClassOption,
    ): Trip {
        delay(600)
        val trip = MockData.sampleActiveTrip(status = TripStatus.REQUESTED).copy(
            pickup = pickup,
            dropoff = dropoff,
            vehicleClass = option.vehicleClass,
            fareCents = option.priceCents,
        )
        _activeTrip.value = trip
        startAutoAdvance()
        return trip
    }

    override suspend fun history(): List<Trip> = _history.toList()

    override suspend fun getTrip(id: String): Trip? =
        _history.firstOrNull { it.id == id } ?: _activeTrip.value?.takeIf { it.id == id }

    override suspend fun cancelTrip(tripId: String) {
        autoAdvanceJob?.cancel()
        driverTrailJob?.cancel()
        val cancelled = _activeTrip.value?.copy(status = TripStatus.CANCELLED)
        if (cancelled != null) _history.add(0, cancelled)
        _activeTrip.value = null
        _driverLocation.value = null
    }

    override suspend fun addStop(tripId: String, stop: Place): Trip {
        delay(250)
        val current = _activeTrip.value ?: throw IllegalStateException("No active trip")
        val newStops = current.stops + stop
        // Fake re-estimate: bump fare by R15 per stop and add 4 min duration
        val updated = current.copy(
            stops = newStops,
            fareCents = current.fareCents + 1_500,
            durationMinutes = current.durationMinutes + 4,
        )
        _activeTrip.value = updated
        return updated
    }

    override suspend fun rateDriver(submission: RatingSubmission) {
        delay(300)
        // Mock: no-op. In real impl this hits POST /trips/{id}/rate
    }

    // ----- auto-advance machinery -----

    private fun startAutoAdvance() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = scope.launch {
            // REQUESTED -> ACCEPTED (driver found)
            delay(5_000)
            transition(TripStatus.ACCEPTED)

            // ACCEPTED -> DRIVER_ARRIVING
            delay(5_000)
            transition(TripStatus.DRIVER_ARRIVING)

            // DRIVER_ARRIVING -> ARRIVED
            delay(5_000)
            transition(TripStatus.ARRIVED)

            // ARRIVED -> IN_PROGRESS (driver starts after ~6s)
            delay(6_000)
            transition(TripStatus.IN_PROGRESS)
            startDriverTrail()

            // IN_PROGRESS -> COMPLETED (simulate driving for ~15s)
            delay(15_000)
            driverTrailJob?.cancel()
            transition(TripStatus.COMPLETED)

            // Move to history, clear active after a beat so UI can celebrate
            delay(4_000)
            val done = _activeTrip.value
            if (done != null && done.status == TripStatus.COMPLETED) {
                _history.add(0, done)
                _activeTrip.value = null
                _driverLocation.value = null
            }
        }
    }

    private fun transition(to: TripStatus) {
        _activeTrip.value = _activeTrip.value?.copy(status = to)
    }

    private fun startDriverTrail() {
        driverTrailJob?.cancel()
        driverTrailJob = scope.launch {
            val trip = _activeTrip.value ?: return@launch
            val start = trip.pickup.location
            val end = trip.dropoff.location
            val steps = 30
            var i = 0
            while (isActive && i <= steps) {
                val t = i.toDouble() / steps
                val lat = start.lat + (end.lat - start.lat) * t
                val lng = start.lng + (end.lng - start.lng) * t
                _driverLocation.value = LatLng(lat, lng)
                delay(500)
                i++
            }
        }
    }

    /** Kept for the "advance (mock)" button on trip-in-progress. */
    suspend fun advanceTripStatus() {
        val current = _activeTrip.value ?: return
        val next = when (current.status) {
            TripStatus.REQUESTED       -> TripStatus.ACCEPTED
            TripStatus.ACCEPTED        -> TripStatus.DRIVER_ARRIVING
            TripStatus.DRIVER_ARRIVING -> TripStatus.ARRIVED
            TripStatus.ARRIVED         -> TripStatus.IN_PROGRESS
            TripStatus.IN_PROGRESS     -> TripStatus.COMPLETED
            else -> return
        }
        _activeTrip.value = current.copy(status = next)
        if (next == TripStatus.IN_PROGRESS) startDriverTrail()
    }
}
