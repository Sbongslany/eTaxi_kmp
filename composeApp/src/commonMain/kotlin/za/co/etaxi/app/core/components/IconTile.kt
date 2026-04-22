package za.co.etaxi.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * A tinted, rounded-square tile holding an icon.
 * This is the signature visual element of the reference screenshots —
 * every destination, every profile row, every service has one.
 *
 * The tint color determines BOTH the background (at 12% alpha) AND the
 * icon stroke color (at full alpha). Use it to thread meaning:
 *   • Blue for identity, personal info
 *   • Amber for notifications
 *   • Power (lime) for payments
 *   • Plum for promo codes
 *   • Ruby for safety
 *   • Cyan for info
 */
@Composable
fun TintedIconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

/**
 * For rows where you want the icon tile + a subtle colored ring.
 * Used in the screenshot's onboarding timeline.
 */
@Composable
fun RingedIconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.14f))
            .background(EtaxiColors.Ink700, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
