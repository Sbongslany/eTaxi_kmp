package za.co.etaxi.app.core.util

/**
 * Formats a cents amount as a South African Rand string, e.g. 24_550 -> "R 245.50".
 * KMP-friendly — no java.util.Locale or NumberFormat.
 */
fun formatRand(cents: Int): String {
    val rands = cents / 100
    val remainder = (cents % 100).toString().padStart(2, '0')
    val withThousands = rands.toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
    return "R $withThousands.$remainder"
}

fun formatDistance(km: Double): String {
    val rounded = (km * 10).toInt() / 10.0
    return "${rounded} km"
}

fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
