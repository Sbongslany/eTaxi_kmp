package za.co.etaxi.app.core.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * The pill at the top of a flow — "Hire Confirmed", "Finding driver…",
 * "Trip in progress".
 *
 * Optional pulsing dot — for live states (finding, searching, etc.).
 */
@Composable
fun HeroStatusPill(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    showPulsingDot: Boolean = true,
) {
    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-a",
    )
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPulsingDot) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = pulseAlpha)),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
        )
    }
}
