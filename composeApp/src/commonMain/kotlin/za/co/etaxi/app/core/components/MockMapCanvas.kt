package za.co.etaxi.app.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Stylized city map — a honest-to-god mock, but styled enough to read as
 * "a map" at a glance: grid streets, faint block fills, a primary route,
 * optional pickup/dropoff pins, and a pulsing driver pin that drifts along
 * the route.
 *
 * This buys us a good-looking Home screen without pulling in Google Maps.
 * Swap for maps-compose on Android / MapKit on iOS during real integration.
 */
@Composable
fun MockMapCanvas(
    modifier: Modifier = Modifier,
    showPickup: Boolean = false,
    showDropoff: Boolean = false,
    showDriver: Boolean = false,
    animateDriver: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "map")
    val tDriver by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateDriver) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(8_000, easing = LinearEasing)),
        label = "driver",
    )
    val tPulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val tShimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3_000, easing = LinearEasing)),
        label = "shimmer",
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base background
            drawRect(EtaxiColors.SlidesDeep)

            // Faint block fills (perceived city blocks)
            val blockCols = 8
            val blockRows = 12
            val bw = w / blockCols
            val bh = h / blockRows
            for (c in 0 until blockCols) {
                for (r in 0 until blockRows) {
                    // Deterministic pseudo-random pattern
                    val seed = (c * 31 + r * 17) % 100
                    if (seed < 55) {
                        val alpha = 0.04f + (seed % 5) * 0.01f
                        drawRect(
                            color = EtaxiColors.Ink.copy(alpha = alpha),
                            topLeft = Offset(c * bw + 2f, r * bh + 2f),
                            size = androidx.compose.ui.geometry.Size(bw - 4f, bh - 4f),
                        )
                    }
                }
            }

            // Grid streets (horizontal)
            val streetColor = EtaxiColors.SlidesEdge.copy(alpha = 0.6f)
            for (i in 1 until blockRows) {
                drawLine(
                    color = streetColor,
                    start = Offset(0f, i * bh),
                    end = Offset(w, i * bh),
                    strokeWidth = 1.5f,
                )
            }
            // Grid streets (vertical)
            for (i in 1 until blockCols) {
                drawLine(
                    color = streetColor,
                    start = Offset(i * bw, 0f),
                    end = Offset(i * bw, h),
                    strokeWidth = 1.5f,
                )
            }

            // Diagonal "highway" for visual interest
            drawLine(
                color = EtaxiColors.SlidesSoft.copy(alpha = 0.7f),
                start = Offset(0f, h * 0.8f),
                end = Offset(w, h * 0.1f),
                strokeWidth = 4f,
                cap = StrokeCap.Round,
            )

            // Primary route (pickup -> dropoff) styled lime
            if (showPickup && showDropoff) {
                val pickupOffset = Offset(w * 0.25f, h * 0.7f)
                val dropoffOffset = Offset(w * 0.75f, h * 0.28f)
                val midA = Offset(w * 0.35f, h * 0.55f)
                val midB = Offset(w * 0.6f, h * 0.42f)
                val path = Path().apply {
                    moveTo(pickupOffset.x, pickupOffset.y)
                    cubicTo(midA.x, midA.y, midB.x, midB.y, dropoffOffset.x, dropoffOffset.y)
                }

                // Glow underlay
                drawPath(
                    path = path,
                    color = EtaxiColors.Power.copy(alpha = 0.2f),
                    style = Stroke(width = 14f, cap = StrokeCap.Round),
                )
                drawPath(
                    path = path,
                    color = EtaxiColors.Power,
                    style = Stroke(width = 5f, cap = StrokeCap.Round),
                )

                // Pickup pin (green-lime)
                drawCircle(
                    color = EtaxiColors.Slides,
                    radius = 14f,
                    center = pickupOffset,
                )
                drawCircle(
                    color = EtaxiColors.Power,
                    radius = 10f,
                    center = pickupOffset,
                    style = Stroke(width = 3f),
                )

                // Dropoff pin (solid lime square-ish)
                val dr = 12f
                drawRect(
                    color = EtaxiColors.Power,
                    topLeft = Offset(dropoffOffset.x - dr, dropoffOffset.y - dr),
                    size = androidx.compose.ui.geometry.Size(dr * 2, dr * 2),
                )

                // Animated driver pin along the route
                if (showDriver) {
                    // Simple cubic lerp using the curve parameters
                    val t = tDriver
                    val drvX = cubicLerp(pickupOffset.x, midA.x, midB.x, dropoffOffset.x, t)
                    val drvY = cubicLerp(pickupOffset.y, midA.y, midB.y, dropoffOffset.y, t)
                    // Pulse halo
                    drawCircle(
                        color = EtaxiColors.Power.copy(alpha = 0.3f),
                        radius = 22f * tPulse,
                        center = Offset(drvX, drvY),
                    )
                    drawCircle(
                        color = EtaxiColors.Power,
                        radius = 11f,
                        center = Offset(drvX, drvY),
                    )
                    drawCircle(
                        color = EtaxiColors.InkOnPower,
                        radius = 5f,
                        center = Offset(drvX, drvY),
                    )
                }
            }

            // Subtle top gradient shimmer for liveness
            drawLine(
                color = EtaxiColors.Power.copy(alpha = 0.06f),
                start = Offset(w * tShimmer, 0f),
                end = Offset(w * tShimmer, h),
                strokeWidth = 80f,
            )
        }
    }
}

private fun cubicLerp(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
    val u = 1 - t
    return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3
}
