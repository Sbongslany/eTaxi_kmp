package za.co.etaxi.app.data.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.etaxi.app.domain.model.CardBrand
import za.co.etaxi.app.domain.model.PaymentMethod
import za.co.etaxi.app.domain.repository.PaymentRepository

class MockPaymentRepository : PaymentRepository {

    private val _methods = MutableStateFlow(MockExtras.paymentMethods)

    override suspend fun methods(): List<PaymentMethod> = _methods.value

    override suspend fun defaultMethod(): PaymentMethod? =
        _methods.value.firstOrNull { it.isDefault }

    override suspend fun addCard(
        brand: String, last4: String, expMonth: Int, expYear: Int,
    ): PaymentMethod {
        delay(600)
        val mapped = when (brand.lowercase()) {
            "visa"       -> CardBrand.VISA
            "mastercard" -> CardBrand.MASTERCARD
            "amex"       -> CardBrand.AMEX
            else         -> CardBrand.OTHER
        }
        val method = PaymentMethod(
            id = "pm-${_methods.value.size + 1}",
            brand = mapped,
            last4 = last4,
            expiryMonth = expMonth,
            expiryYear = expYear,
            isDefault = _methods.value.isEmpty(),
        )
        _methods.value = _methods.value + method
        return method
    }

    override suspend fun setDefault(methodId: String) {
        _methods.value = _methods.value.map { it.copy(isDefault = it.id == methodId) }
    }

    override suspend fun removeCard(methodId: String) {
        _methods.value = _methods.value.filterNot { it.id == methodId }
    }

    override suspend fun confirmTripPayment(
        tripId: String,
        methodId: String,
    ): Result<Unit> {
        delay(1_200) // feel like a Stripe call
        return Result.success(Unit)
    }
}
