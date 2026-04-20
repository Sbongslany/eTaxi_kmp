package za.co.etaxi.app.domain.repository
import kotlinx.coroutines.flow.Flow
import za.co.etaxi.app.domain.model.AppNotification
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.PaymentMethod
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.User
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleClassOption
import za.co.etaxi.app.domain.model.WalletTransaction

interface SessionRepository {
    val currentUser: Flow<User?>
    val selectedRole: Flow<UserRole?>
    suspend fun pickRole(role: UserRole)
    suspend fun login(identifier: String, password: String): Result<User>
    suspend fun register(
        email: String, phone: String, password: String,
        firstName: String, lastName: String, role: UserRole,
    ): Result<User>
    suspend fun verifyOtp(code: String): Result<Unit>
    suspend fun logout()
    suspend fun updateProfile(firstName: String, lastName: String, email: String, phone: String): Result<User>
    suspend fun changePassword(current: String, next: String): Result<Unit>
}

interface RiderRepository {
    suspend fun savedPlaces(): List<Place>
    suspend fun estimate(pickup: Place, dropoff: Place): List<VehicleClassOption>
    fun activeTrip(): Flow<Trip?>
    fun driverLocation(): Flow<LatLng?>
    suspend fun requestTrip(pickup: Place, dropoff: Place, option: VehicleClassOption): Trip
    suspend fun history(): List<Trip>
    suspend fun getTrip(id: String): Trip?
    suspend fun cancelTrip(tripId: String)
    suspend fun addStop(tripId: String, stop: Place): Trip
    suspend fun rateDriver(submission: RatingSubmission)
}

interface DriverRepository {
    val isOnline: Flow<Boolean>
    suspend fun setOnline(online: Boolean)
    fun incomingRequests(): Flow<DriverIncomingRequest?>
    fun activeTrip(): Flow<Trip?>
    suspend fun acceptRequest(tripId: String): Trip
    suspend fun declineRequest(tripId: String)
    suspend fun markArriving(tripId: String)
    suspend fun markArrived(tripId: String)
    suspend fun startTrip(tripId: String)
    suspend fun endTrip(tripId: String)
    suspend fun history(): List<Trip>
    suspend fun earningsSummary(): DriverEarnings
    suspend fun rateRider(submission: RatingSubmission)
    suspend fun pushLocation(tripId: String, location: LatLng)
    suspend fun vehicles(): List<DriverVehicle>
    suspend fun addVehicle(
        make: String, model: String, year: Int, color: String,
        plateNumber: String, vehicleClass: VehicleClass,
    ): DriverVehicle
    suspend fun setActiveVehicle(vehicleId: String)
    suspend fun documents(): List<DriverDocument>
    suspend fun uploadDocument(type: DocumentType): DriverDocument
    suspend fun walletTransactions(): List<WalletTransaction>
}

data class DriverEarnings(
    val todayCents: Int,
    val todayTrips: Int,
    val weekCents: Int,
    val weekTrips: Int,
    val monthCents: Int,
    val monthTrips: Int,
    val walletCents: Int,
)

interface PaymentRepository {
    suspend fun methods(): List<PaymentMethod>
    suspend fun defaultMethod(): PaymentMethod?
    suspend fun addCard(brand: String, last4: String, expMonth: Int, expYear: Int): PaymentMethod
    suspend fun setDefault(methodId: String)
    suspend fun removeCard(methodId: String)
    suspend fun confirmTripPayment(tripId: String, methodId: String): Result<Unit>
}

interface NotificationRepository {
    fun all(): Flow<List<AppNotification>>
    suspend fun markRead(id: String)
    suspend fun markAllRead()
}
