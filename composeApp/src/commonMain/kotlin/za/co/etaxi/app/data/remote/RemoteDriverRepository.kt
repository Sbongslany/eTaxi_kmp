package za.co.etaxi.app.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import za.co.etaxi.app.core.network.AddVehicleRequest
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.DocumentDto
import za.co.etaxi.app.core.network.DriverProfileDto
import za.co.etaxi.app.core.network.GoOnlineRequest
import za.co.etaxi.app.core.network.SetActiveVehicleRequest
import za.co.etaxi.app.core.network.UpdateLocationRequest
import za.co.etaxi.app.core.network.UploadDocumentRequest
import za.co.etaxi.app.core.network.UploadUrlRequest
import za.co.etaxi.app.core.network.UploadUrlResponse
import za.co.etaxi.app.core.network.VehicleDto
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleStatus
import za.co.etaxi.app.domain.model.WalletTransaction
import za.co.etaxi.app.domain.repository.DriverEarnings
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository

class RemoteDriverRepository(
    private val api: RemoteDriverApi,
) : DriverRepository {

    private val _isOnline = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _onboardingState = MutableStateFlow(DriverOnboardingState.NOT_STARTED)
    override val onboardingState: StateFlow<DriverOnboardingState> = _onboardingState.asStateFlow()

    private val _incoming = MutableStateFlow<DriverIncomingRequest?>(null)
    private val _activeTrip = MutableStateFlow<Trip?>(null)

    // Tracks which vehicle the backend currently considers "active" so the
    // VehicleDto.toDomain() mapper can set isActive without needing extra
    // fields in the DTO.
    private var activeVehicleId: String? = null

    override fun incomingRequests(): Flow<DriverIncomingRequest?> = _incoming.asStateFlow()
    override fun activeTrip(): Flow<Trip?> = _activeTrip.asStateFlow()

    suspend fun refreshOnboarding() {
        val res = api.me()
        if (res is ApiResult.Success<*>) {
            val profile = res.value as DriverProfileDto
            activeVehicleId = profile.activeVehicle?.id
            _onboardingState.value = computeOnboardingState(profile)
            _isOnline.value = profile.availability == "ONLINE" || profile.availability == "ON_TRIP"
        }
    }

    override suspend fun setOnline(online: Boolean) {
        if (online) {
            val res = api.goOnline(GoOnlineRequest(lat = -26.2041, lng = 28.0473))
            if (res is ApiResult.Success<*>) _isOnline.value = true
        } else {
            val res = api.goOffline()
            if (res is ApiResult.Success<*>) _isOnline.value = false
        }
    }

    override suspend fun vehicles(): List<DriverVehicle> {
        val res = api.me()
        return if (res is ApiResult.Success<*>) {
            val profile = res.value as DriverProfileDto
            activeVehicleId = profile.activeVehicle?.id
            listOfNotNull(profile.activeVehicle?.toDomain())
        } else emptyList()
    }

    override suspend fun addVehicle(
        make: String, model: String, year: Int, color: String,
        plateNumber: String, vehicleClass: VehicleClass,
    ): DriverVehicle {
        val req = AddVehicleRequest(
            // Backend now accepts the code (STANDARD / XL / LUX) and resolves
            // it to a real VehicleType UUID server-side, per backend Patch K.
            vehicleTypeId = vehicleClass.name,
            make = make.trim(),
            model = model.trim(),
            year = year,
            color = color.trim(),
            plateNumber = plateNumber.uppercase().trim(),
        )
        val res = api.addVehicle(req)
        if (res is ApiResult.Success<*>) {
            val vehicle = (res.value as VehicleDto).toDomain(requestedClass = vehicleClass)
            refreshOnboarding()
            return vehicle
        }
        val failure = res as ApiResult.Failure
        throw Exception("Add vehicle failed: ${failure.message}")
    }

    override suspend fun setActiveVehicle(vehicleId: String) {
        api.setActiveVehicle(SetActiveVehicleRequest(vehicleId = vehicleId))
        refreshOnboarding()
    }

    override suspend fun documents(): List<DriverDocument> {
        // The backend includes documents in /drivers/me but our DriverProfileDto
        // doesn't currently surface them. Return empty for now; the onboarding
        // wizard's "documents" step uses local repo state pre-upload.
        return emptyList()
    }

    override suspend fun uploadDocument(type: DocumentType): DriverDocument {
        val uploadRes = api.getUploadUrl(
            UploadUrlRequest(
                folder = "documents",
                contentType = "image/jpeg",
                filename = "${type.name.lowercase()}.jpg",
            )
        )
        val publicUrl = if (uploadRes is ApiResult.Success<*>) {
            val u = uploadRes.value as UploadUrlResponse
            u.publicUrl ?: u.uploadUrl.substringBefore("?")
        } else {
            val failure = uploadRes as ApiResult.Failure
            throw Exception("Upload URL failed: ${failure.message}")
        }

        val docRes = api.uploadDocument(
            UploadDocumentRequest(type = type.name, url = publicUrl)
        )
        if (docRes is ApiResult.Success<*>) {
            val doc = (docRes.value as DocumentDto).toDomain(fallbackType = type)
            refreshOnboarding()
            return doc
        }
        val failure = docRes as ApiResult.Failure
        throw Exception("Document upload failed: ${failure.message}")
    }

    override suspend fun submitOnboarding() {
        refreshOnboarding()
    }

    override suspend fun resetForNewDriver() {
        _onboardingState.value = DriverOnboardingState.NOT_STARTED
        _isOnline.value = false
        activeVehicleId = null
    }

    override suspend fun earningsSummary(): DriverEarnings = DriverEarnings(
        todayCents = 0, todayTrips = 0,
        weekCents = 0, weekTrips = 0,
        monthCents = 0, monthTrips = 0,
        walletCents = 0,
    )

    override suspend fun walletTransactions(): List<WalletTransaction> = emptyList()

    override suspend fun acceptRequest(tripId: String): Trip =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun declineRequest(tripId: String) =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun markArriving(tripId: String) =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun markArrived(tripId: String) =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun startTrip(tripId: String) =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun endTrip(tripId: String) =
        throw NotImplementedError("Trip endpoints land in the next patch")
    override suspend fun history(): List<Trip> = emptyList()
    override suspend fun rateRider(submission: RatingSubmission) = Unit

    override suspend fun pushLocation(tripId: String, location: LatLng) {
        api.updateLocation(UpdateLocationRequest(lat = location.lat, lng = location.lng))
    }

    // ─────────────────────────────── Mappers ───────────────────────────────

    // VehicleDto -> DriverVehicle
    //
    // Notes on tricky fields:
    //
    //  - vehicleClass: The backend stores vehicleTypeId as a UUID. The mobile
    //    enum has STANDARD / XL / LUX. We can't infer the enum from a UUID
    //    alone (without an extra API call). When the mapper is called from
    //    addVehicle() we pass the enum the user picked as a hint. Otherwise
    //    we fall back to STANDARD — visually correct for the most common
    //    case, and refreshOnboarding() will replace the value once the user
    //    sees their full profile.
    //
    //  - status: VehicleDto.status is a String matching the enum names. If
    //    parsing fails we default to PENDING since that's the post-create
    //    state until the admin approves.
    //
    //  - isActive: comes from this.activeVehicleId which we cache from the
    //    last /drivers/me response.
    private fun VehicleDto.toDomain(
        requestedClass: VehicleClass? = null,
    ): DriverVehicle = DriverVehicle(
        id = id,
        make = make,
        model = model,
        year = year,
        color = color,
        plateNumber = plateNumber,
        vehicleClass = requestedClass
            ?: runCatching { VehicleClass.valueOf(vehicleTypeId.uppercase()) }
                .getOrDefault(VehicleClass.STANDARD),
        status = runCatching { VehicleStatus.valueOf(status) }
            .getOrDefault(VehicleStatus.PENDING),
        isActive = activeVehicleId != null && activeVehicleId == id,
    )

    // DocumentDto -> DriverDocument
    //
    // Note: domain DriverDocument does NOT carry id or url fields. The
    // backend keeps both for storage; the UI only cares about type + status
    // + timestamps. We surface uploadedAt from issuedAt (or fall back to
    // null) and leave expiresAt / rejectionReason pass-through.
    private fun DocumentDto.toDomain(fallbackType: DocumentType): DriverDocument =
        DriverDocument(
            type = runCatching { DocumentType.valueOf(type) }.getOrDefault(fallbackType),
            status = runCatching { DocumentStatus.valueOf(status) }
                .getOrDefault(DocumentStatus.PENDING),
            uploadedAt = issuedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            expiresAt = expiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            rejectionReason = rejectionReason,
        )

    private fun computeOnboardingState(profile: DriverProfileDto): DriverOnboardingState =
        when (profile.status) {
            "VERIFIED" -> DriverOnboardingState.APPROVED
            "SUSPENDED", "DEACTIVATED" -> DriverOnboardingState.REJECTED
            else -> if (profile.activeVehicle == null) DriverOnboardingState.NOT_STARTED
                    else DriverOnboardingState.IN_PROGRESS
        }
}
