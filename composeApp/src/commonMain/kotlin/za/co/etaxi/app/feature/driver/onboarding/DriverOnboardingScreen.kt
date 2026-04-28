package za.co.etaxi.app.feature.driver.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository

// Four-step driver onboarding wizard.
//
// Resume-aware: on entry we fetch the driver's current vehicle + documents
// from the backend. The wizard resumes at the first incomplete step:
//
//   No vehicle yet                    -> WELCOME
//   Vehicle saved, missing required   -> DOCUMENTS
//   Vehicle + all required submitted  -> SUBMITTED
//   Backend says VERIFIED already     -> SUBMITTED (HomeScreen will route them to shell)
//
// Without resume-on-load, drivers who close the app mid-flow lose their
// progress visually even though the backend has it. With resume, they
// pick up exactly where they left off.
object DriverOnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val onboardState by repo.onboardingState.collectAsState(
            initial = DriverOnboardingState.NOT_STARTED,
        )

        var step by remember { mutableStateOf<OnboardStep?>(null) }

        // Compute the right resume step on entry.
        // We rely on repo.vehicles() and repo.documents() which both hit
        // GET /drivers/me; the calls are cheap and need to happen exactly
        // once. After that, normal step transitions are driven by the
        // child screens via their onComplete/onSubmitted callbacks.
        LaunchedEffect(Unit) {
            val vehicleCount = runCatching { repo.vehicles().size }.getOrDefault(0)
            val docs = runCatching { repo.documents() }.getOrDefault(emptyList())
            val requiredTypes = DocumentType.entries.filter { it.required }
            val submittedRequired = docs.count {
                it.type.required && it.status != DocumentStatus.MISSING
            }

            step = when {
                vehicleCount == 0 -> OnboardStep.WELCOME
                submittedRequired < requiredTypes.size -> OnboardStep.DOCUMENTS
                else -> OnboardStep.SUBMITTED
            }
        }

        // If the backend later flips state to AWAITING_REVIEW or APPROVED
        // while the user sits in any step, jump them to SUBMITTED.
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
            val current = step
            if (current == null) {
                // Loading shimmer — shows briefly while we fetch the resume step
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Loading your progress...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EtaxiColors.InkMuted,
                    )
                }
                return@Column
            }

            ProgressHeader(
                step = current,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            )

            AnimatedContent(
                targetState = current,
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
