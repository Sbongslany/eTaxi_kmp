package za.co.etaxi.app.core.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * eTaxi motion system.
 *
 * Every motion on the app should come from one of these specs so the
 * overall feel is cohesive rather than ad-hoc. Bolt/Uber-premium means
 * motion is CONFIDENT — slightly faster than you'd expect, with
 * subtle spring overshoot on user-initiated actions to reward them.
 *
 * Guidelines:
 *   • UI transitions (fade, slide): Tween with `FastOutSlowInEasing` — 250ms.
 *   • User-initiated state changes (tap, toggle): Spring `Snappy` — delights.
 *   • Entrances (screens, sheets): Spring `Gentle` — feels floated in.
 *   • Continuous indicators (progress, loading): Tween linear.
 */
object EtaxiMotion {

    // ===== Spring specs =====

    /** Snappy — visible overshoot. Use for user-initiated toggles, taps, stars. */
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    /** Gentle — almost no overshoot, floated-in feel. Use for sheets, screens. */
    val Gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /** Stiff — almost instant response, no wobble. Use when snapping to a state. */
    val Stiff = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessHigh,
    )

    // ===== Tween specs =====

    /** Short tween for UI fade/slide. Use when spring would feel too organic. */
    fun <T> fast()   = tween<T>(durationMillis = 200, easing = FastOutSlowInEasing)
    fun <T> medium() = tween<T>(durationMillis = 320, easing = FastOutSlowInEasing)
    fun <T> slow()   = tween<T>(durationMillis = 480, easing = LinearOutSlowInEasing)

    // ===== Durations (ms) for manual use =====
    const val DurFast   = 200
    const val DurMedium = 320
    const val DurSlow   = 480
}
