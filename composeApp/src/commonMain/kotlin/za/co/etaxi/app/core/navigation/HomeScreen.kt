package za.co.etaxi.app.core.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.driver.DriverShellScreen
import za.co.etaxi.app.feature.driver.onboarding.DriverOnboardingScreen
import za.co.etaxi.app.feature.rider.RiderShellScreen

/**
 * Root router. Looks at the current role and, for drivers, their onboarding
 * state, to decide which shell to show.
 *
 * Strict driver gate:
 *   NOT_STARTED / IN_PROGRESS  → onboarding wizard
 *   AWAITING_REVIEW / APPROVED → shell (app)
 *   REJECTED                   → onboarding wizard (so they can fix rejected items)
 */
object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val session: SessionRepository = koinInject()
        val driverRepo: DriverRepository = koinInject()

        val role by session.selectedRole.collectAsState(initial = null)
        val onboarding by driverRepo.onboardingState.collectAsState(
            initial = DriverOnboardingState.APPROVED,
        )

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
