package za.co.etaxi.app.feature.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.data.remote.RemoteSessionRepository
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.login.LoginScreen

// Splash screen — app entry point.
// On launch:
//   1. Show the brand animation briefly.
//   2. If the repo is the Remote impl, try to restore a session
//      (reads stored token + calls GET /auth/me).
//   3. If session restored -> HomeScreen (which does role-based routing).
//      Otherwise -> LoginScreen.
//
// RoleSelectionScreen is intentionally bypassed. Role is picked inside
// RegisterScreen directly.
object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()

        var show by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            show = true
            // Try to restore session while the splash plays.
            val restored = (session as? RemoteSessionRepository)
                ?.runCatching { restoreSession() }
                ?.getOrDefault(false)
                ?: false
            delay(1_400)
            if (restored) {
                navigator.replaceAll(HomeScreen)
            } else {
                navigator.replaceAll(LoginScreen)
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (show) 1f else 0.6f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 180f),
            label = "logoScale",
        )
        val alpha by animateFloatAsState(
            targetValue = if (show) 1f else 0f,
            animationSpec = tween(700),
            label = "logoAlpha",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.SlidesDeep),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(24.dp))
                        .background(EtaxiColors.Power),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "eT",
                        color = EtaxiColors.InkOnPower,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "eTaxi",
                    style = MaterialTheme.typography.displayMedium,
                    color = EtaxiColors.Power,
                    modifier = Modifier.alpha(alpha),
                )
            }
        }
    }
}
