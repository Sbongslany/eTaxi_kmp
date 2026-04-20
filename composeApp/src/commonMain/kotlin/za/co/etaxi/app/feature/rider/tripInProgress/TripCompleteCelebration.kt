package za.co.etaxi.app.feature.rider.tripInProgress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloat
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiSecondaryButton
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand

/**
 * Full-screen celebration overlay shown after trip completes.
 * - Lime halo pulses behind a check mark
 * - Fare counter tallies up from 0 to the final amount
 * - "Rate your driver" primary CTA
 * - "Dismiss" secondary
 */
@Composable
fun TripCompleteCelebration(
    visible: Boolean,
    fareCents: Int,
    distanceKm: Double,
    durationMinutes: Int,
    onRate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                // Pulsing halo + check
                val inf = rememberInfiniteTransition(label = "halo")
                val halo by inf.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(tween(1_400)),
                    label = "h",
                )
                val haloAlpha by inf.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(1_400)),
                    label = "ha",
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(halo)
                            .clip(CircleShape)
                            .background(EtaxiColors.Power.copy(alpha = haloAlpha)),
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(EtaxiColors.Power),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = EtaxiColors.InkOnPower,
                            modifier = Modifier.size(72.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Trip complete",
                    style = MaterialTheme.typography.displaySmall,
                    color = EtaxiColors.Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Thanks for riding with us.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.InkMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))

                // Animated fare counter
                val progress by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow,
                    ),
                    label = "tally",
                )
                val shownCents = (fareCents * progress).toInt()

                Text(
                    formatRand(shownCents),
                    style = MaterialTheme.typography.displayLarge,
                    color = EtaxiColors.Power,
                )

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatCell("Distance", "$distanceKm km")
                    StatCell("Time", "$durationMinutes min")
                }

                Spacer(Modifier.height(32.dp))
                EtaxiPrimaryButton(
                    text = "Rate your driver",
                    onClick = onRate,
                )
                Spacer(Modifier.height(12.dp))
                EtaxiSecondaryButton(
                    text = "Dismiss",
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
    }
}
