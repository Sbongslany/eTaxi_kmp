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
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.driver.DriverShellScreen
import za.co.etaxi.app.feature.rider.RiderShellScreen

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val session: SessionRepository = koinInject()
        val role by session.selectedRole.collectAsState(initial = null)
        AnimatedContent(
            targetState = role,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            label = "role-shell",
        ) { r ->
            when (r) {
                UserRole.DRIVER -> DriverShellScreen.Content()
                UserRole.RIDER  -> RiderShellScreen.Content()
                null -> RiderShellScreen.Content() // sensible default
            }
        }
    }
}
