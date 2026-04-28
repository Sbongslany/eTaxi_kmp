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
import za.co.etaxi.app.core.upload.FileUploader
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
            val source = profile.vehicles.ifEmpty { listOfNotNull(profile.activeVehicle) }
            source.map { it.toDomain() }
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
        val backendByType: Map<DocumentType, DocumentDto> = run {
            val res = api.me()
            if (res !is ApiResult.Success<*>) return@run emptyMap()
            val profile = res.value as DriverProfileDto
            profile.documents
                .mapNotNull { dto ->
                    val type = runCatching { DocumentType.valueOf(dto.type) }.getOrNull()
                    if (type != null) type to dto else null
                }
                .toMap()
        }
        return DocumentType.entries.map { type ->
            val dto = backendByType[type]
            if (dto != null) {
                dto.toDomain(fallbackType = type)
            } else {
                DriverDocument(type = type, status = DocumentStatus.MISSING)
            }
        }
    }

    // Interface contract: kept as a no-op throw to make accidental
    // mock-style calls fail loudly. Real upload uses uploadDocumentWithBytes.
    override suspend fun uploadDocument(type: DocumentType): DriverDocument {
        throw IllegalStateException(
            "Use uploadDocumentWithBytes(type, bytes) instead — " +
                "the real backend requires bytes uploaded to a presigned URL."
        )
    }

    // Real upload flow:
    //   1. POST /storage/upload-url -> { uploadUrl, publicUrl }
    //   2. PUT bytes to uploadUrl (S3 etc.) with the right contentType
    //   3. POST /drivers/documents { type, url: publicUrl }
    //   4. refreshOnboarding() so the driver state reflects the new doc
    //
    // The DocumentsStep composable calls this directly (it has a
    // RemoteDriverRepository-typed reference because Koin returns the
    // concrete type for additional methods like this one).
    suspend fun uploadDocumentWithBytes(
        type: DocumentType,
        bytes: ByteArray,
        contentType: String = "image/jpeg",
        filenameSuffix: String = "jpg",
    ): DriverDocument {
        // Step 1: presigned URL
        val uploadRes = api.getUploadUrl(
            UploadUrlRequest(
                folder = "documents",
                contentType = contentType,
                filename = "${type.name.lowercase()}.${filenameSuffix}",
            )
        )
        val uploadResponse: UploadUrlResponse = if (uploadRes is ApiResult.Success<*>) {
            uploadRes.value as UploadUrlResponse
        } else {
            val failure = uploadRes as ApiResult.Failure
            throw Exception("Upload URL failed: ${failure.message}")
        }

        // Step 2: PUT bytes to S3
        val putOk = FileUploader.uploadBytes(
            presignedUrl = uploadResponse.uploadUrl,
            contentType = contentType,
            bytes = bytes,
        )
        if (!putOk) {
            throw Exception("Uploading file bytes to storage failed")
        }

        // Step 3: register the document with the backend
        val publicUrl = uploadResponse.publicUrl
            ?: uploadResponse.uploadUrl.substringBefore("?")
        val docRes = api.uploadDocument(
            UploadDocumentRequest(type = type.name, url = publicUrl)
        )
        if (docRes is ApiResult.Success<*>) {
            val doc = (docRes.value as DocumentDto).toDomain(fallbackType = type)
            refreshOnboarding()
            return doc
        }
        val failure = docRes as ApiResult.Failure
        throw Exception("Document register failed: ${failure.message}")
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

    private fun DocumentDto.toDomain(fallbackType: DocumentType): DriverDocument =
        DriverDocument(
            type = runCatching { DocumentType.valueOf(type) }.getOrDefault(fallbackType),
            status = runCatching { DocumentStatus.valueOf(status) }
                .getOrDefault(DocumentStatus.PENDING),
            uploadedAt = issuedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            expiresAt = expiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            rejectionReason = rejectionReason,
        )

    // Derives the driver's onboarding state from their profile.
    //
    // VERIFIED      -> APPROVED. Backend admin has approved them.
    // SUSPENDED /
    // DEACTIVATED  -> REJECTED. Will route them back into the wizard.
    // PENDING (default) drilling deeper:
    //   * No active vehicle yet -> NOT_STARTED.
    //   * Active vehicle but no docs uploaded -> IN_PROGRESS.
    //   * Active vehicle AND every required document is uploaded
    //     (any non-MISSING status counts) -> AWAITING_REVIEW. This is
    //     the state HomeScreen checks to send the driver into the
    //     DriverShell rather than back into the wizard.
    private fun computeOnboardingState(profile: DriverProfileDto): DriverOnboardingState {
        return when (profile.status) {
            "VERIFIED" -> DriverOnboardingState.APPROVED
            "SUSPENDED", "DEACTIVATED" -> DriverOnboardingState.REJECTED
            else -> {
                val hasVehicle = profile.activeVehicle != null
                if (!hasVehicle) return DriverOnboardingState.NOT_STARTED

                // All seven required document types must have been
                // submitted (i.e. exist on the backend, not necessarily
                // approved). Profile photo is the only optional one.
                val uploadedRequired: Set<String> = profile.documents
                    .map { it.type }
                    .toSet()
                val requiredTypes: Set<String> = setOf(
                    "ID_DOCUMENT",
                    "DRIVERS_LICENSE",
                    "PRDP",
                    "VEHICLE_REGISTRATION",
                    "ROADWORTHY",
                    "OPERATING_LICENSE",
                    "INSURANCE",
                )
                val allRequiredSubmitted = requiredTypes.all { it in uploadedRequired }
                if (allRequiredSubmitted) DriverOnboardingState.AWAITING_REVIEW
                else DriverOnboardingState.IN_PROGRESS
            }
        }
    }
}
