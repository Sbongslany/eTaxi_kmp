package za.co.etaxi.app.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.VehicleClass
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

    override fun incomingRequests(): Flow<DriverIncomingRequest?> = _incoming.asStateFlow()
    override fun activeTrip(): Flow<Trip?> = _activeTrip.asStateFlow()

    suspend fun refreshOnboarding() {
        val res = api.me()
        if (res is ApiResult.Success<*>) {
            val profile = res.value as DriverProfileDto
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
            listOfNotNull(profile.activeVehicle?.toDomain())
        } else emptyList()
    }

    override suspend fun addVehicle(
        make: String, model: String, year: Int, color: String,
        plateNumber: String, vehicleClass: VehicleClass,
    ): DriverVehicle {
        val req = AddVehicleRequest(
            vehicleTypeId = vehicleClass.name,
            make = make.trim(),
            model = model.trim(),
            year = year,
            color = color.trim(),
            plateNumber = plateNumber.uppercase().trim(),
        )
        val res = api.addVehicle(req)
        if (res is ApiResult.Success<*>) {
            refreshOnboarding()
            return (res.value as VehicleDto).toDomain()
        }
        val failure = res as ApiResult.Failure
        throw Exception("Add vehicle failed: ${failure.message}")
    }

    override suspend fun setActiveVehicle(vehicleId: String) {
        api.setActiveVehicle(SetActiveVehicleRequest(vehicleId = vehicleId))
        refreshOnboarding()
    }

    override suspend fun documents(): List<DriverDocument> = emptyList()

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
            refreshOnboarding()
            return (docRes.value as DocumentDto).toDomain(type)
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

    private fun VehicleDto.toDomain(): DriverVehicle = throw NotImplementedError("Vehicle mapping wired in next patch")

    private fun DocumentDto.toDomain(fallbackType: DocumentType): DriverDocument = throw NotImplementedError("Document mapping wired in next patch")

    private fun computeOnboardingState(profile: DriverProfileDto): DriverOnboardingState =
        when (profile.status) {
            "VERIFIED" -> DriverOnboardingState.APPROVED
            "SUSPENDED", "DEACTIVATED" -> DriverOnboardingState.REJECTED
            else -> if (profile.activeVehicle == null) DriverOnboardingState.NOT_STARTED
                    else DriverOnboardingState.IN_PROGRESS
        }
}
