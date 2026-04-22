package za.co.etaxi.app.feature.driver.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository

/**
 * Four-step driver onboarding wizard.
 *
 * Flow: Welcome → Vehicle → Documents → Submitted.
 *
 * Strict gate: cannot be dismissed or sent to background. Only way out is
 * to complete all steps, which flips onboardingState to AWAITING_REVIEW.
 * HomeScreen then stops redirecting back here.
 */
object DriverOnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val onboardState by repo.onboardingState.collectAsState(
            initial = DriverOnboardingState.NOT_STARTED,
        )

        var step by remember { mutableStateOf(OnboardStep.WELCOME) }

        // When state becomes AWAITING_REVIEW, move to the final step automatically.
        LaunchedEffect(onboardState) {
            if (onboardState == DriverOnboardingState.AWAITING_REVIEW ||
                onboardState == DriverOnboardingState.APPROVED
            ) {
                step = OnboardStep.SUBMITTED
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ProgressHeader(
                step = step,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            )

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "onboard-step",
                modifier = Modifier.weight(1f),
            ) { s ->
                when (s) {
                    OnboardStep.WELCOME -> WelcomeStep(
                        onStart = { step = OnboardStep.VEHICLE },
                    )
                    OnboardStep.VEHICLE -> VehicleStep(
                        onComplete = { step = OnboardStep.DOCUMENTS },
                    )
                    OnboardStep.DOCUMENTS -> DocumentsStep(
                        onBack = { step = OnboardStep.VEHICLE },
                        onSubmitted = { step = OnboardStep.SUBMITTED },
                    )
                    OnboardStep.SUBMITTED -> SubmittedStep(
                        onContinue = { nav.replaceAll(HomeScreen) },
                    )
                }
            }
        }
    }
}

enum class OnboardStep { WELCOME, VEHICLE, DOCUMENTS, SUBMITTED }

@Composable
private fun ProgressHeader(step: OnboardStep, modifier: Modifier = Modifier) {
    val index = when (step) {
        OnboardStep.WELCOME   -> 0
        OnboardStep.VEHICLE   -> 1
        OnboardStep.DOCUMENTS -> 2
        OnboardStep.SUBMITTED -> 3
    }
    val total = 4
    val progress by animateFloatAsState(
        targetValue = (index + 1).toFloat() / total.toFloat(),
        animationSpec = tween(400),
        label = "progress",
    )
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Step ${index + 1} of $total",
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkFaint,
                modifier = Modifier.weight(1f),
            )
            Text(
                when (step) {
                    OnboardStep.WELCOME   -> "Welcome"
                    OnboardStep.VEHICLE   -> "Your vehicle"
                    OnboardStep.DOCUMENTS -> "Your documents"
                    OnboardStep.SUBMITTED -> "All set"
                },
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.InkMuted,
            )
        }
        Spacer(Modifier.height(8.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(EtaxiColors.SlidesRaised),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(EtaxiColors.Power),
            )
        }
    }
}
