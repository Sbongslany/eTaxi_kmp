package za.co.etaxi.app.data.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleStatus
import za.co.etaxi.app.domain.model.WalletTransaction
import za.co.etaxi.app.domain.repository.DriverEarnings
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository

class MockDriverRepository : DriverRepository {

    private val _isOnline = MutableStateFlow(false)
    override val isOnline = _isOnline.asStateFlow()

    private val _incoming = MutableStateFlow<DriverIncomingRequest?>(null)
    override fun incomingRequests() = _incoming.asStateFlow()

    private val _active = MutableStateFlow<Trip?>(null)
    override fun activeTrip() = _active.asStateFlow()

    private val _vehicles = MutableStateFlow(MockExtras.driverVehicles)
    private val _documents = MutableStateFlow(MockExtras.driverDocuments)
    private val _transactions = MutableStateFlow(MockExtras.walletTransactions)

    private val _onboardingState = MutableStateFlow(deriveOnboardingState(
        MockExtras.driverVehicles,
        MockExtras.driverDocuments,
    ))
    override val onboardingState = _onboardingState.asStateFlow()

    override suspend fun setOnline(online: Boolean) {
        _isOnline.value = online
        if (online) {
            delay(2_500)
            if (_isOnline.value && _active.value == null) {
                _incoming.value = MockData.sampleIncomingRequest
            }
        } else {
            _incoming.value = null
        }
    }

    override suspend fun acceptRequest(tripId: String): Trip {
        val req = _incoming.value ?: throw IllegalStateException("No request to accept")
        val trip = MockData.sampleActiveTrip(TripStatus.ACCEPTED).copy(
            id = req.tripId,
            pickup = req.pickup,
            dropoff = req.dropoff,
            fareCents = req.fareCents,
            distanceKm = req.distanceKm,
            durationMinutes = req.durationMinutes,
        )
        _active.value = trip
        _incoming.value = null
        return trip
    }

    override suspend fun declineRequest(tripId: String) {
        _incoming.value = null
    }

    override suspend fun markArriving(tripId: String) = transitionActive(TripStatus.DRIVER_ARRIVING)
    override suspend fun markArrived(tripId: String)  = transitionActive(TripStatus.ARRIVED)
    override suspend fun startTrip(tripId: String)    = transitionActive(TripStatus.IN_PROGRESS)

    override suspend fun endTrip(tripId: String) {
        _active.value = _active.value?.copy(status = TripStatus.COMPLETED)
        delay(400)
        _active.value = null
    }

    private fun transitionActive(next: TripStatus) {
        _active.value = _active.value?.copy(status = next)
    }

    override suspend fun history(): List<Trip> = MockData.driverHistory

    override suspend fun earningsSummary() = DriverEarnings(
        todayCents = 68_450,
        todayTrips = 9,
        weekCents = 412_300,
        weekTrips = 54,
        monthCents = 1_780_500,
        monthTrips = 221,
        walletCents = 212_650,
    )

    // ===== New =====

    override suspend fun rateRider(submission: RatingSubmission) {
        delay(300) // pretend network
    }

    override suspend fun pushLocation(tripId: String, location: LatLng) {
        // Mock: in real impl this hits POST /trips/{id}/location
    }

    override suspend fun vehicles(): List<DriverVehicle> = _vehicles.value

    override suspend fun addVehicle(
        make: String, model: String, year: Int, color: String,
        plateNumber: String, vehicleClass: VehicleClass,
    ): DriverVehicle {
        delay(400)
        val vehicle = DriverVehicle(
            id = "v-${_vehicles.value.size + 1}",
            make = make, model = model, year = year, color = color,
            plateNumber = plateNumber,
            vehicleClass = vehicleClass,
            status = VehicleStatus.PENDING,
        )
        _vehicles.value = _vehicles.value + vehicle
        recomputeOnboarding()
        return vehicle
    }

    override suspend fun setActiveVehicle(vehicleId: String) {
        _vehicles.value = _vehicles.value.map {
            it.copy(isActive = it.id == vehicleId)
        }
        recomputeOnboarding()
    }

    override suspend fun documents(): List<DriverDocument> = _documents.value

    override suspend fun uploadDocument(type: DocumentType): DriverDocument {
        delay(600)
        val updated = DriverDocument(
            type = type,
            status = DocumentStatus.PENDING,
            uploadedAt = kotlinx.datetime.Clock.System.now(),
        )
        _documents.value = _documents.value.map { if (it.type == type) updated else it }
            .toMutableList()
            .also { if (it.none { d -> d.type == type }) it.add(updated) }
        recomputeOnboarding()
        return updated
    }

    override suspend fun walletTransactions(): List<WalletTransaction> = _transactions.value

    // ===== Onboarding =====

    override suspend fun resetForNewDriver() {
        _vehicles.value = emptyList()
        _documents.value = DocumentType.entries.map { type ->
            DriverDocument(type = type, status = DocumentStatus.MISSING)
        }
        _onboardingState.value = DriverOnboardingState.NOT_STARTED
    }

    override suspend fun submitOnboarding() {
        // In real backend: POST /drivers/submit → status becomes AWAITING_REVIEW.
        // Mock: if everything is present, flip to AWAITING_REVIEW; otherwise stay IN_PROGRESS.
        val vehiclesOk = _vehicles.value.isNotEmpty()
        val requiredDocsUploaded = DocumentType.entries.filter { it.required }
            .all { type ->
                _documents.value.any { it.type == type && it.status != DocumentStatus.MISSING }
            }
        if (vehiclesOk && requiredDocsUploaded) {
            _onboardingState.value = DriverOnboardingState.AWAITING_REVIEW
        }
    }

    private fun recomputeOnboarding() {
        _onboardingState.value = deriveOnboardingState(_vehicles.value, _documents.value)
    }
}

/**
 * Derives the driver's onboarding state from current vehicle and document lists.
 *
 * NOT_STARTED     → no vehicles at all
 * IN_PROGRESS     → has vehicles but missing any required doc upload
 * AWAITING_REVIEW → set by `submitOnboarding()` only
 * APPROVED        → one vehicle APPROVED + all required docs APPROVED
 * REJECTED        → any required doc or the active vehicle REJECTED
 */
private fun deriveOnboardingState(
    vehicles: List<DriverVehicle>,
    documents: List<DriverDocument>,
): DriverOnboardingState {
    if (vehicles.isEmpty()) return DriverOnboardingState.NOT_STARTED

    val requiredDocs = DocumentType.entries.filter { it.required }
    val statusByType = documents.associateBy { it.type }

    val anyRejected = requiredDocs.any {
        statusByType[it]?.status == DocumentStatus.REJECTED
    } || vehicles.any { it.status == VehicleStatus.REJECTED }
    if (anyRejected) return DriverOnboardingState.REJECTED

    val allApproved = vehicles.any { it.status == VehicleStatus.APPROVED } &&
        requiredDocs.all { statusByType[it]?.status == DocumentStatus.APPROVED }
    if (allApproved) return DriverOnboardingState.APPROVED

    val missingAny = requiredDocs.any {
        val s = statusByType[it]?.status
        s == null || s == DocumentStatus.MISSING
    }
    return if (missingAny) DriverOnboardingState.IN_PROGRESS
    else DriverOnboardingState.AWAITING_REVIEW
}
