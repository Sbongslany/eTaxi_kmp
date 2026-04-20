package za.co.etaxi.app.feature.rider.rate

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.DriverPreview
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.repository.RiderRepository

class RateDriverScreen(
    private val tripId: String,
    private val driverName: String,
    private val fareLabel: String,
) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: RiderRepository = koinInject()
        val scope = rememberCoroutineScope()

        var stars by remember { mutableStateOf(0) }
        var comment by remember { mutableStateOf("") }
        var submitting by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Fare chip
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(EtaxiColors.PowerGlow)
                    .border(1.dp, EtaxiColors.Power, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    "Paid $fareLabel",
                    style = MaterialTheme.typography.labelLarge,
                    color = EtaxiColors.Power,
                )
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Power),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    driverName.split(" ").map { it.firstOrNull() ?: ' ' }.joinToString("").take(2),
                    style = MaterialTheme.typography.displayMedium,
                    color = EtaxiColors.InkOnPower,
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "How was your ride with\n$driverName?",
                style = MaterialTheme.typography.headlineMedium,
                color = EtaxiColors.Ink,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // Star row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(5) { i ->
                    val isLit = i < stars
                    val scale by animateFloatAsState(
                        if (isLit) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "star",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isLit) EtaxiColors.PowerGlow
                                else MaterialTheme.colorScheme.surface,
                            )
                            .border(
                                1.dp,
                                if (isLit) EtaxiColors.Power else EtaxiColors.SlidesEdge,
                                RoundedCornerShape(16.dp),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { stars = i + 1 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = "${i + 1} stars",
                            tint = if (isLit) EtaxiColors.Power else EtaxiColors.InkFaint,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }

            if (stars > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    when (stars) {
                        1 -> "Let us know what went wrong"
                        2 -> "Could be better"
                        3 -> "It was okay"
                        4 -> "Great ride"
                        else -> "Excellent — top rating!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.InkMuted,
                )
            }

            Spacer(Modifier.height(32.dp))

            EtaxiTextField(
                value = comment,
                onValueChange = { comment = it },
                label = "Leave a comment (optional)",
                placeholder = "What stood out?",
                singleLine = false,
            )

            Spacer(Modifier.weight(1f))

            EtaxiPrimaryButton(
                text = if (stars == 0) "Skip" else "Submit rating",
                loading = submitting,
                enabled = !submitting,
                onClick = {
                    if (stars == 0) {
                        nav.pop()
                        return@EtaxiPrimaryButton
                    }
                    scope.launch {
                        submitting = true
                        repo.rateDriver(
                            RatingSubmission(
                                tripId = tripId,
                                stars = stars,
                                comment = comment.ifBlank { null },
                            ),
                        )
                        submitting = false
                        nav.pop()
                    }
                },
            )
        }
    }

    companion object {
        fun from(tripId: String, driver: DriverPreview?, fareLabel: String) =
            RateDriverScreen(
                tripId = tripId,
                driverName = driver?.name ?: "your driver",
                fareLabel = fareLabel,
            )
    }
}
