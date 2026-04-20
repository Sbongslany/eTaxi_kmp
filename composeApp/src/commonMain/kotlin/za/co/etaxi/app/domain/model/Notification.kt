package za.co.etaxi.app.domain.model

import kotlinx.datetime.Instant

enum class NotificationKind {
    TRIP,
    PAYMENT,
    PROMO,
    SYSTEM,
}

data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val read: Boolean = false,
)
