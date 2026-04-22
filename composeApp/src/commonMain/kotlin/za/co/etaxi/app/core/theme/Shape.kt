package za.co.etaxi.app.core.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * eTaxi shape system v2 — slightly rounder across the board, matching the
 * softer, more premium Bolt/Uber direction while keeping angular
 * signage-poster brand identity.
 */
val EtaxiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),    // tiny chips, pills inside cards
    small      = RoundedCornerShape(12.dp),   // inputs, small buttons
    medium     = RoundedCornerShape(16.dp),   // standard cards, sheet rows
    large      = RoundedCornerShape(20.dp),   // large cards
    extraLarge = RoundedCornerShape(28.dp),   // hero cards, bottom sheets
)

object EtaxiRadii {
    val none   = RoundedCornerShape(0.dp)
    val xs     = RoundedCornerShape(8.dp)
    val sm     = RoundedCornerShape(12.dp)
    val md     = RoundedCornerShape(16.dp)
    val lg     = RoundedCornerShape(20.dp)
    val xl     = RoundedCornerShape(24.dp)
    val xxl    = RoundedCornerShape(28.dp)
    /** Bottom-sheet-style: rounded top only, flat bottom. */
    val sheet  = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    /** Fully pill shape — use for chips and dot badges. */
    val pill   = CircleShape
}
