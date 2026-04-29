package za.co.etaxi.app.core.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import za.co.etaxi.app.data.remote.RemoteDriverRepository
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.driver.DriverShellScreen
import za.co.etaxi.app.feature.driver.onboarding.DriverOnboardingScreen
import za.co.etaxi.app.feature.rider.RiderShellScreen

// Root router. Looks at the current role and, for drivers, their onboarding
// state, to decide which shell to show.
//
// Driver state -> destination:
//   NOT_STARTED / IN_PROGRESS / REJECTED  -> onboarding wizard
//   AWAITING_REVIEW / APPROVED            -> shell
//
// Patch Q: on entry we explicitly call refreshOnboarding() so the
// driver's StateFlow has the latest backend snapshot before we route.
// Previously, HomeScreen could read a stale value cached from much
// earlier in the session and bounce a freshly-submitted driver back
// into the wizard. We also block routing until the refresh completes
// to avoid a brief flash of the wizard before the shell appears.
object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val session: SessionRepository = koinInject()
        val driverRepo: DriverRepository = koinInject()

        val role by session.selectedRole.collectAsState(initial = null)
        val onboarding by driverRepo.onboardingState.collectAsState(
            initial = DriverOnboardingState.APPROVED,
        )

        // Refresh once when this screen mounts AND the user is a driver.
        // Calling refreshOnboarding() for a rider hits /drivers/me which
        // returns 403 and just adds noise to the backend log. Riders
        // never need driver onboarding state at all.
        var refreshed by remember { mutableStateOf(false) }
        LaunchedEffect(role) {
            if (role == UserRole.DRIVER) {
                (driverRepo as? RemoteDriverRepository)
                    ?.runCatching { refreshOnboarding() }
            }
            refreshed = true
        }

        if (!refreshed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
            return
        }

        val target = when (role) {
            UserRole.DRIVER -> when (onboarding) {
                DriverOnboardingState.NOT_STARTED,
                DriverOnboardingState.IN_PROGRESS,
                DriverOnboardingState.REJECTED -> "driver-onboarding"
                DriverOnboardingState.AWAITING_REVIEW,
                DriverOnboardingState.APPROVED -> "driver-shell"
            }
            UserRole.RIDER -> "rider-shell"
            null           -> "rider-shell"
        }

        AnimatedContent(
            targetState = target,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            label = "home-route",
        ) { t ->
            when (t) {
                "driver-onboarding" -> DriverOnboardingScreen.Content()
                "driver-shell"      -> DriverShellScreen.Content()
                else                -> RiderShellScreen.Content()
            }
        }
    }
}
