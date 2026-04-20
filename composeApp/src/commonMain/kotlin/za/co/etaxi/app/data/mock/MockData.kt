package za.co.etaxi.app.data.mock

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import za.co.etaxi.app.domain.model.AppNotification
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverPreview
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.NotificationKind
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.model.User
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleClassOption

/** Hardcoded data used across the app until the backend is integrated. */
object MockData {

    val now: Instant get() = Clock.System.now()

    val riderUser = User(
        id = "rider-1",
        firstName = "Xolani",
        lastName = "Ndebele",
        email = "xolani@example.com",
        phone = "+27823456789",
        role = UserRole.RIDER,
        rating = 4.91,
        totalTrips = 42,
    )

    val driverUser = User(
        id = "driver-1",
        firstName = "Thando",
        lastName = "Mokoena",
        email = "thando@example.com",
        phone = "+27829876543",
        role = UserRole.DRIVER,
        rating = 4.96,
        totalTrips = 1_247,
    )

    val savedPlaces = listOf(
        Place("Home",   "12 Helen Rd, Sandhurst, Sandton",           LatLng(-26.1058, 28.0528)),
        Place("Work",   "158 Jan Smuts Ave, Rosebank",               LatLng(-26.1420, 28.0410)),
        Place("Gym",    "Virgin Active, Melrose Arch",               LatLng(-26.1340, 28.0640)),
        Place("Airport","OR Tambo International, Kempton Park",      LatLng(-26.1337, 28.2411)),
    )

    val popularPlaces = listOf(
        Place("Sandton City Mall",     "83 Rivonia Rd, Sandhurst",       LatLng(-26.1080, 28.0560)),
        Place("Rosebank Gautrain",     "177 Oxford Rd, Rosebank",        LatLng(-26.1466, 28.0430)),
        Place("Maboneng Precinct",     "264 Fox St, City and Suburban", LatLng(-26.2050, 28.0570)),
        Place("OR Tambo International","Kempton Park",                   LatLng(-26.1337, 28.2411)),
        Place("Melville 7th Street",   "7th St, Melville",               LatLng(-26.1770, 28.0083)),
    )

    fun estimate(
        pickup: Place,
        dropoff: Place,
    ): List<VehicleClassOption> {
        // Fake haversine + fare calc — this is only for UI
        val dx = pickup.location.lat - dropoff.location.lat
        val dy = pickup.location.lng - dropoff.location.lng
        val approxKm = (kotlin.math.sqrt(dx * dx + dy * dy) * 111).coerceAtLeast(1.0)
        val base = (2500 + approxKm * 950).toInt()
        return listOf(
            VehicleClassOption(VehicleClass.STANDARD, priceCents = base),
            VehicleClassOption(VehicleClass.XL,       priceCents = (base * 1.45).toInt()),
            VehicleClassOption(VehicleClass.LUX,      priceCents = (base * 2.10).toInt()),
        )
    }

    val defaultDriverPreview = DriverPreview(
        id = "driver-1",
        name = "Thando M.",
        rating = 4.96,
        vehicleModel = "VW Polo Vivo",
        plateNumber = "GP 483 284",
    )

    fun sampleActiveTrip(status: TripStatus = TripStatus.ACCEPTED): Trip = Trip(
        id = "trip-active",
        status = status,
        pickup = savedPlaces[0],
        dropoff = savedPlaces[3],
        vehicleClass = VehicleClass.STANDARD,
        fareCents = 24_550,
        distanceKm = 31.4,
        durationMinutes = 38,
        requestedAt = now,
        driver = defaultDriverPreview,
    )

    val riderHistory = listOf(
        Trip(
            id = "trip-1",
            status = TripStatus.COMPLETED,
            pickup = savedPlaces[0],
            dropoff = savedPlaces[1],
            vehicleClass = VehicleClass.STANDARD,
            fareCents = 8450,
            distanceKm = 7.2,
            durationMinutes = 18,
            requestedAt = now,
            driver = defaultDriverPreview,
        ),
        Trip(
            id = "trip-2",
            status = TripStatus.COMPLETED,
            pickup = savedPlaces[1],
            dropoff = popularPlaces[2],
            vehicleClass = VehicleClass.XL,
            fareCents = 15_200,
            distanceKm = 12.8,
            durationMinutes = 28,
            requestedAt = now,
            driver = DriverPreview("d2", "Nomsa D.", 4.82, "Toyota Quantum", "GP 112 998"),
        ),
        Trip(
            id = "trip-3",
            status = TripStatus.CANCELLED,
            pickup = savedPlaces[0],
            dropoff = savedPlaces[3],
            vehicleClass = VehicleClass.LUX,
            fareCents = 0,
            distanceKm = 0.0,
            durationMinutes = 0,
            requestedAt = now,
        ),
        Trip(
            id = "trip-4",
            status = TripStatus.COMPLETED,
            pickup = savedPlaces[2],
            dropoff = savedPlaces[0],
            vehicleClass = VehicleClass.STANDARD,
            fareCents = 5_620,
            distanceKm = 4.1,
            durationMinutes = 11,
            requestedAt = now,
            driver = DriverPreview("d3", "Sipho M.", 4.88, "Hyundai Atos", "GP 771 554"),
        ),
    )

    val driverHistory = listOf(
        Trip(
            id = "dtrip-1",
            status = TripStatus.COMPLETED,
            pickup = popularPlaces[0],
            dropoff = popularPlaces[1],
            vehicleClass = VehicleClass.STANDARD,
            fareCents = 6_750,
            distanceKm = 5.4,
            durationMinutes = 14,
            requestedAt = now,
            driver = defaultDriverPreview,
        ),
        Trip(
            id = "dtrip-2",
            status = TripStatus.COMPLETED,
            pickup = popularPlaces[2],
            dropoff = popularPlaces[3],
            vehicleClass = VehicleClass.STANDARD,
            fareCents = 22_800,
            distanceKm = 29.1,
            durationMinutes = 36,
            requestedAt = now,
            driver = defaultDriverPreview,
        ),
        Trip(
            id = "dtrip-3",
            status = TripStatus.COMPLETED,
            pickup = popularPlaces[4],
            dropoff = popularPlaces[1],
            vehicleClass = VehicleClass.STANDARD,
            fareCents = 9_340,
            distanceKm = 8.0,
            durationMinutes = 21,
            requestedAt = now,
            driver = defaultDriverPreview,
        ),
    )

    val sampleIncomingRequest = DriverIncomingRequest(
        tripId = "trip-active",
        riderName = "Xolani N.",
        riderRating = 4.91,
        pickup = savedPlaces[0],
        dropoff = savedPlaces[3],
        distanceKm = 31.4,
        durationMinutes = 38,
        fareCents = 24_550,
        pickupDistanceKm = 1.2,
        expiresAtSeconds = 15,
    )

    val notifications = listOf(
        AppNotification(
            id = "n1",
            kind = NotificationKind.PROMO,
            title = "20% off your next 3 rides",
            body = "Weekend promo: 20% off rides under R50. Valid Fri–Sun.",
            createdAt = now,
            read = false,
        ),
        AppNotification(
            id = "n2",
            kind = NotificationKind.TRIP,
            title = "Rate your last trip",
            body = "How was your ride with Thando? Your feedback keeps us moving.",
            createdAt = now,
            read = false,
        ),
        AppNotification(
            id = "n3",
            kind = NotificationKind.PAYMENT,
            title = "Payment received",
            body = "R84.50 charged to Visa •• 4242 for trip to Rosebank.",
            createdAt = now,
            read = true,
        ),
        AppNotification(
            id = "n4",
            kind = NotificationKind.SYSTEM,
            title = "Welcome to eTaxi",
            body = "Your account is verified. Enjoy the ride.",
            createdAt = now,
            read = true,
        ),
    )
}
