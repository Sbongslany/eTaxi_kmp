package za.co.etaxi.app.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

@Composable
fun PulsingDot(
    color: Color = EtaxiColors.Power,
    size: androidx.compose.ui.unit.Dp = 12.dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale",
    )
    Box(modifier = modifier.size(size).scale(scale)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = color)
        }
    }
}

@Composable
fun ThreeDotLoader(
    color: Color = EtaxiColors.Power,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val transition = rememberInfiniteTransition(label = "dot$i")
            val alpha by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(650, delayMillis = i * 150, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "alpha$i",
            )
            Canvas(Modifier.size(8.dp)) {
                drawCircle(color = color.copy(alpha = alpha))
            }
        }
    }
}

/**
 * Expanding ring — perfect for "searching for drivers" UI.
 */
@Composable
fun PulseRing(
    color: Color = EtaxiColors.Power,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        repeat(3) { i ->
            val transition = rememberInfiniteTransition(label = "ring$i")
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(1800, delayMillis = i * 600, easing = LinearEasing),
                ),
                label = "p$i",
            )
            Canvas(Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2f) * progress
                val alpha = (1f - progress).coerceIn(0f, 1f) * 0.6f
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
        }
        Canvas(Modifier.size(18.dp)) { drawCircle(color = color) }
    }
}
