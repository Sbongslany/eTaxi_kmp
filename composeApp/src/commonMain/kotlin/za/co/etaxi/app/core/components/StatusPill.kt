package za.co.etaxi.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * A small colored pill for statuses — "APPROVED", "PENDING", "ACTIVE" etc.
 * Optional dot prefix that can pulse for "live" states.
 */
@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showDot) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

/**
 * Small uppercase metadata label. Use above a value — "FROM", "TOTAL FARE",
 * "STATUS" etc. Never use for anything the user reads as regular prose.
 */
@Composable
fun EyebrowLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EtaxiColors.BoneFaint,
) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
    )
}
