package za.co.etaxi.app.feature.driver.rate

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.RatingSubmission
import za.co.etaxi.app.domain.repository.DriverRepository

class RateRiderScreen(
    private val tripId: String,
    private val riderName: String,
    private val fareLabel: String,
) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
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
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(EtaxiColors.PowerGlow)
                    .border(1.dp, EtaxiColors.Power, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    "Earned $fareLabel",
                    style = MaterialTheme.typography.labelLarge,
                    color = EtaxiColors.Power,
                )
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.SlidesRaised),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    riderName.split(" ").map { it.firstOrNull() ?: ' ' }.joinToString("").take(2),
                    style = MaterialTheme.typography.displayMedium,
                    color = EtaxiColors.Ink,
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "How was your rider\n$riderName?",
                style = MaterialTheme.typography.headlineMedium,
                color = EtaxiColors.Ink,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

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
                            contentDescription = null,
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
                        1 -> "Something went wrong"
                        2 -> "Not great"
                        3 -> "Okay rider"
                        4 -> "Good rider"
                        else -> "Excellent rider"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.InkMuted,
                )
            }

            Spacer(Modifier.height(32.dp))

            EtaxiTextField(
                value = comment,
                onValueChange = { comment = it },
                label = "Add a note (optional)",
                placeholder = "Anything else to flag?",
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
                        repo.rateRider(RatingSubmission(tripId, stars, comment.ifBlank { null }))
                        submitting = false
                        nav.pop()
                    }
                },
            )
        }
    }
}
