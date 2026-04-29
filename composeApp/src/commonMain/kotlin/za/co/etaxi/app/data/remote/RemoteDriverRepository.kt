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
import za.co.etaxi.app.core.network.AddVehicleRequest
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.DocumentDto
import za.co.etaxi.app.core.network.DriverProfileDto
import za.co.etaxi.app.core.network.GoOnlineRequest
import za.co.etaxi.app.core.network.LocationPingBody
import za.co.etaxi.app.core.network.RatingBody
import za.co.etaxi.app.core.network.SetActiveVehicleRequest
import za.co.etaxi.app.core.network.TripDto
import za.co.etaxi.app.core.network.UpdateLocationRequest
import za.co.etaxi.app.core.network.UploadDocumentRequest
import za.co.etaxi.app.core.network.UploadUrlRequest
import za.co.etaxi.app.core.network.UploadUrlResponse
import za.co.etaxi.app.core.network.VehicleDto
import za.co.etaxi.app.core.upload.FileUploader
import za.co.etaxi.app.core.ws.TripEvent
import za.co.etaxi.app.core.ws.WsClient
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverPreview
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleStatus
import za.co.etaxi.app.domain.model.WalletTransaction
import za.co.etaxi.app.domain.repository.DriverEarnings
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository

class RemoteDriverRepository(
    private val api: RemoteDriverApi,
    private val tripApi: RemoteTripApi,
    private val ws: WsClient,
    private val locationStreamer: za.co.etaxi.app.core.location.LocationStreamer,
) : DriverRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isOnline = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _onboardingState = MutableStateFlow(DriverOnboardingState.NOT_STARTED)
    override val onboardingState: StateFlow<DriverOnboardingState> = _onboardingState.asStateFlow()

    // Most recent ride request that the driver has not yet accepted/declined.
    // Driven by NEW_RIDE_REQUEST WS events.
    private val _incoming = MutableStateFlow<DriverIncomingRequest?>(null)

    // Active trip the driver is currently on. Set when they accept,
    // updated on each status transition, cleared when the trip ends.
    private val _activeTrip = MutableStateFlow<Trip?>(null)

    private var activeVehicleId: String? = null

    init {
        ws.connect()
        scope.launch {
            ws.events.collect { event -> onWsEvent(event) }
        }
    }

    override fun incomingRequests(): Flow<DriverIncomingRequest?> = _incoming.asStateFlow()
    override fun activeTrip(): Flow<Trip?> = _activeTrip.asStateFlow()

    // ================= Profile / online =================
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
            // Try real GPS first. If permission is denied or no fix is
            // available within 5s, fall back to a coord close to the
            // rider's default saved pickup ("Home" / 12 Helen Rd, Sandton
            // at -26.1076, 28.0567) so demo dispatch works on emulators
            // and devices without a recent GPS fix. The matcher's default
            // search radius is small (~5 km), so going online far from
            // the rider's pickup means the dispatcher returns "no
            // drivers available".
            val pos = locationStreamer.lastKnownLocation()
                ?: LatLng(-26.1076, 28.0567)
            val res = api.goOnline(GoOnlineRequest(lat = pos.lat, lng = pos.lng))
            if (res is ApiResult.Success<*>) _isOnline.value = true
        } else {
            val res = api.goOffline()
            if (res is ApiResult.Success<*>) _isOnline.value = false
        }
    }

    // ================= Vehicles =================
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
            // Auto-activate the vehicle. The backend's addVehicle service
            // doesn't set activeVehicleId on its own, which means the
            // driver can't go online until something does. We set it
            // here so the rest of the onboarding flow has a usable
            // active vehicle reference. If the driver already has a
            // different active vehicle this is a no-op-ish overwrite,
            // which matches "newest vehicle becomes the working one"
            // semantics.
            runCatching {
                api.setActiveVehicle(SetActiveVehicleRequest(vehicleId = vehicle.id))
            }
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

    // ================= Documents =================
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

    override suspend fun uploadDocument(type: DocumentType): DriverDocument {
        throw IllegalStateException(
            "Use uploadDocumentWithBytes(type, bytes) instead - " +
                "the real backend requires bytes uploaded to a presigned URL."
        )
    }

    suspend fun uploadDocumentWithBytes(
        type: DocumentType,
        bytes: ByteArray,
        contentType: String = "image/jpeg",
        filenameSuffix: String = "jpg",
    ): DriverDocument {
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

        val putOk = FileUploader.uploadBytes(
            presignedUrl = uploadResponse.uploadUrl,
            contentType = contentType,
            bytes = bytes,
        )
        if (!putOk) throw Exception("Uploading file bytes to storage failed")

        val publicUrl = uploadResponse.publicUrl ?: uploadResponse.uploadUrl.substringBefore("?")
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

    // ================= Trip lifecycle =================
    // The driver's active trip and incoming requests are managed jointly
    // by HTTP responses (when the driver acts) and WS events (when the
    // server pushes updates). After a successful HTTP call we set the
    // local state so the UI updates immediately without waiting for the
    // WS round-trip.

    override suspend fun acceptRequest(tripId: String): Trip {
        val res = tripApi.accept(tripId)
        if (res is ApiResult.Success<*>) {
            val trip = (res.value as TripDto).toTripDomain()
            _activeTrip.value = trip
            _incoming.value = null
            return trip
        }
        val failure = res as ApiResult.Failure
        throw Exception("Accept failed: ${failure.message}")
    }

    override suspend fun declineRequest(tripId: String) {
        // Backend has no explicit decline endpoint — declining just
        // means the driver doesn't accept, the dispatcher hands it to
        // the next candidate. We just clear the local incoming request.
        if (_incoming.value?.tripId == tripId) {
            _incoming.value = null
        }
    }

    override suspend fun markArriving(tripId: String) {
        val res = tripApi.arriving(tripId)
        if (res is ApiResult.Success<*>) {
            _activeTrip.value = (res.value as TripDto).toTripDomain()
        }
    }

    override suspend fun markArrived(tripId: String) {
        val res = tripApi.arrived(tripId)
        if (res is ApiResult.Success<*>) {
            _activeTrip.value = (res.value as TripDto).toTripDomain()
        }
    }

    override suspend fun startTrip(tripId: String) {
        val res = tripApi.start(tripId)
        if (res is ApiResult.Success<*>) {
            _activeTrip.value = (res.value as TripDto).toTripDomain()
        }
    }

    override suspend fun endTrip(tripId: String) {
        val res = tripApi.end(tripId)
        if (res is ApiResult.Success<*>) {
            // End response is { trip, fare } but we accept TripDto-shape
            // fallback; refresh from GET /trips/:id to be safe.
            val refreshed = tripApi.get(tripId)
            if (refreshed is ApiResult.Success<*>) {
                _activeTrip.value = (refreshed.value as TripDto).toTripDomain()
            } else {
                _activeTrip.value = (res.value as TripDto).toTripDomain()
            }
        }
    }

    override suspend fun cancelTrip(tripId: String, reason: String?) {
        val res = tripApi.cancel(tripId, reason ?: "Cancelled by driver")
        if (res is ApiResult.Success<*>) {
            _activeTrip.value = null
        }
    }

    override suspend fun addStop(tripId: String, stop: za.co.etaxi.app.domain.model.Place): Trip {
        val res = tripApi.addStop(
            tripId,
            za.co.etaxi.app.core.network.AddStopBody(
                lat = stop.location.lat,
                lng = stop.location.lng,
                address = stop.address,
            )
        )
        return if (res is ApiResult.Success<*>) {
            val refreshed = tripApi.get(tripId)
            val trip = if (refreshed is ApiResult.Success<*>) {
                (refreshed.value as TripDto).toTripDomain()
            } else {
                (res.value as TripDto).toTripDomain()
            }
            _activeTrip.value = trip
            trip
        } else {
            val failure = res as ApiResult.Failure
            throw Exception("Add stop failed: ${failure.message}")
        }
    }

    override suspend fun history(): List<Trip> = emptyList()

    override suspend fun rateRider(submission: RatingSubmission) {
        val active = _activeTrip.value ?: return
        // riderId is on the TripDto, not on DriverPreview. We need a
        // different lookup — fetch the full trip to get riderId.
        val tripRes = tripApi.get(submission.tripId)
        val toUserId = (tripRes as? ApiResult.Success<*>)
            ?.let { (it.value as TripDto).riderId }
            ?: return
        tripApi.rate(
            RatingBody(
                tripId = submission.tripId,
                toUserId = toUserId,
                stars = submission.stars,
                comment = submission.comment,
            )
        )
        if (_activeTrip.value?.id == submission.tripId) {
            _activeTrip.value = null
        }
    }

    override suspend fun pushLocation(tripId: String, location: LatLng) {
        // Two destinations: the driver's overall live location
        // (drivers/location, used for matching) and the per-trip
        // location channel (trips/:id/location, broadcast to the rider).
        api.updateLocation(UpdateLocationRequest(lat = location.lat, lng = location.lng))
        tripApi.location(
            tripId,
            LocationPingBody(lat = location.lat, lng = location.lng),
        )
    }

    // ================== WS event handling ==================
    private suspend fun onWsEvent(event: TripEvent) {
        when (event) {
            is TripEvent.NewRideRequest -> {
                // Show in the driver's incoming-request sheet.
                _incoming.value = DriverIncomingRequest(
                    tripId = event.tripId,
                    riderName = "Rider",
                    riderRating = 5.0,
                    pickup = Place(
                        label = "Pickup",
                        address = event.pickupAddress,
                        location = LatLng(event.pickupLat, event.pickupLng),
                    ),
                    dropoff = Place(
                        label = "Dropoff",
                        address = event.dropoffAddress,
                        location = LatLng(event.dropoffLat, event.dropoffLng),
                    ),
                    distanceKm = 0.0,  // server calculates ride distance, not surfaced here
                    durationMinutes = 0,
                    fareCents = event.estimatedFareCents,
                    pickupDistanceKm = event.pickupDistanceKm,
                )
            }
            is TripEvent.StatusUpdate -> {
                // If this is our active trip, refresh from the server
                // for the freshest data.
                val active = _activeTrip.value
                if (active != null && active.id == event.tripId) {
                    val res = tripApi.get(event.tripId)
                    if (res is ApiResult.Success<*>) {
                        val trip = (res.value as TripDto).toTripDomain()
                        if (trip.status == TripStatus.COMPLETED ||
                            trip.status == TripStatus.CANCELLED
                        ) {
                            // Keep the trip set so the rating screen has
                            // context, but the UI uses status to navigate.
                            _activeTrip.value = trip
                        } else {
                            _activeTrip.value = trip
                        }
                    }
                }
            }
            is TripEvent.DriverLocation -> {
                // Driver doesn't care about its own location updates here.
            }
            is TripEvent.Cancelled -> {
                if (_activeTrip.value?.id == event.tripId) {
                    _activeTrip.value = null
                }
                if (_incoming.value?.tripId == event.tripId) {
                    _incoming.value = null
                }
            }
        }
    }

    // ================== Mappers ==================
    private fun VehicleDto.toDomain(
        requestedClass: VehicleClass? = null,
    ): DriverVehicle = DriverVehicle(
        id = id,
        make = make,
        model = model,
        year = year,
        color = color,
        plateNumber = plateNumber,
        vehicleClass = requestedClass ?: vehicleTypeIdToClass(vehicleTypeId),
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

    private fun TripDto.toTripDomain(): Trip {
        val pickup = Place(
            label = "Pickup",
            address = pickupAddress,
            location = LatLng(pickupLat, pickupLng),
        )
        val dropoff = Place(
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
                vehicleModel = d.activeVehicle?.let { "${it.make} ${it.model}" }
                    ?: vehicle?.let { "${it.make} ${it.model}" }
                    ?: "",
                plateNumber = d.activeVehicle?.plateNumber
                    ?: vehicle?.plateNumber
                    ?: "",
                photoUrl = d.user?.profilePhoto,
            )
        }
        val riderPreview = rider?.let { r ->
            val name = listOfNotNull(r.firstName, r.lastName)
                .joinToString(" ").ifBlank { "Rider" }
            za.co.etaxi.app.domain.model.RiderPreview(
                id = r.id,
                name = name,
                rating = 5.0,
                photoUrl = r.profilePhoto,
            )
        }
        val stopPlaces = stops.map {
            Place(label = "Stop", address = it.address, location = LatLng(it.lat, it.lng))
        }
        return Trip(
            id = id,
            status = parseStatus(status),
            pickup = pickup,
            dropoff = dropoff,
            stops = stopPlaces,
            vehicleClass = cls,
            fareCents = finalFareCents ?: estimatedFareCents,
            distanceKm = (distanceMeters ?: 0) / 1000.0,
            durationMinutes = (durationSeconds ?: 0) / 60,
            requestedAt = parsedRequestedAt,
            driver = driverPreview,
            rider = riderPreview,
        )
    }

    private fun parseStatus(s: String): TripStatus =
        runCatching { TripStatus.valueOf(s) }.getOrDefault(TripStatus.REQUESTED)

    private fun computeOnboardingState(profile: DriverProfileDto): DriverOnboardingState {
        return when (profile.status) {
            "VERIFIED" -> DriverOnboardingState.APPROVED
            "SUSPENDED", "DEACTIVATED" -> DriverOnboardingState.REJECTED
            else -> {
                val hasVehicle = profile.activeVehicle != null
                if (!hasVehicle) return DriverOnboardingState.NOT_STARTED

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

    // Map a backend VehicleType UUID back to the readable enum.
    // The seeded UUIDs are stable; if a re-seed regenerates them,
    // update both this helper and vehicleClassToTypeCode in
    // RemoteRiderRepository.kt.
    private fun vehicleTypeIdToClass(id: String): VehicleClass = when (id) {
        "768bacf6-d177-4af7-9b57-d82fe9d3a0cf" -> VehicleClass.STANDARD
        "6dcc92b7-b6c6-4467-baf6-80840a0a70e5" -> VehicleClass.XL
        "386fb1e5-ec8f-4cc8-b451-b1ad703dfc74" -> VehicleClass.LUX
        else -> runCatching { VehicleClass.valueOf(id.uppercase()) }
            .getOrDefault(VehicleClass.STANDARD)
    }
}
