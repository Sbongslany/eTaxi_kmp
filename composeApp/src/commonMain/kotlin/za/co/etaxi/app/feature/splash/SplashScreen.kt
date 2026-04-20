package za.co.etaxi.app.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.feature.auth.roleSelection.RoleSelectionScreen

object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var show by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            show = true
            delay(1_600)
            navigator.replaceAll(RoleSelectionScreen)
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

        // Rotating background glow
        val transition = rememberInfiniteTransition(label = "splashBg")
        val angle by transition.animateFloat(
            0f, 360f,
            animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
            label = "angle",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.SlidesDeep),
            contentAlignment = Alignment.Center,
        ) {
            // Background halo
            Canvas(Modifier.fillMaxSize().alpha(0.35f)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val rad = kotlin.math.PI.toFloat() / 180f * angle
                drawCircle(
                    color = EtaxiColors.Power.copy(alpha = 0.15f),
                    radius = size.minDimension * 0.45f,
                    center = Offset(cx + 40f * kotlin.math.cos(rad), cy + 40f * kotlin.math.sin(rad)),
                )
                drawCircle(
                    color = EtaxiColors.Power.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.6f,
                    center = Offset(cx - 60f * kotlin.math.cos(rad), cy - 60f * kotlin.math.sin(rad)),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scale).alpha(alpha),
            ) {
                // Brand mark
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(EtaxiColors.Power, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "e",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = EtaxiColors.InkOnPower,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "eTaxi",
                    style = MaterialTheme.typography.displayLarge,
                    color = EtaxiColors.Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "MOVE SMARTER.",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                    color = EtaxiColors.Power,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
