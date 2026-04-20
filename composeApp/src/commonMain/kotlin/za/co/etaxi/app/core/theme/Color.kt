package za.co.etaxi.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * eTaxi brand palette.
 *
 * Anchors from the design reference:
 *  - POWER  = #C3D809 (lime)
 *  - SLIDES = #222022 (near-black surface)
 *
 * The rest is a calibrated ramp around those two for Material 3 surfaces,
 * borders, elevated cards, error states, and secondary/tertiary accents.
 */
object EtaxiColors {
    // ===== Core brand =====
    val Power        = Color(0xFFC3D809)   // primary accent (lime)
    val PowerDim     = Color(0xFFA8BC08)   // hovered/pressed primary
    val PowerMuted   = Color(0xFF6B7A05)   // de-emphasized lime
    val PowerGlow    = Color(0x33C3D809)   // 20% lime for glows & selected chips

    val Slides       = Color(0xFF222022)   // surface
    val SlidesDeep   = Color(0xFF161518)   // background (darker than surface)
    val SlidesRaised = Color(0xFF2B2A2D)   // elevated card surface
    val SlidesEdge   = Color(0xFF3A383D)   // divider / outline
    val SlidesSoft   = Color(0xFF4A484D)   // disabled / placeholder outline

    // ===== Text on dark surfaces =====
    val Ink         = Color(0xFFF5F5F0)   // primary text (slight warm white)
    val InkMuted    = Color(0xFFB6B6B0)   // secondary text
    val InkFaint    = Color(0xFF7A7A75)   // tertiary / helper text
    val InkOnPower  = Color(0xFF0A0A0A)   // text sitting on lime (near-black)

    // ===== Semantic =====
    val Success     = Color(0xFF3DD68C)
    val Warning     = Color(0xFFFFB547)
    val Danger      = Color(0xFFFF5757)
    val DangerDim   = Color(0xFF2A1416)

    // ===== Light mode (fallback if user enables light) =====
    val LightBg         = Color(0xFFF5F5F0)
    val LightSurface    = Color(0xFFFFFFFF)
    val LightInk        = Color(0xFF0A0A0A)
    val LightInkMuted   = Color(0xFF5F5F5A)
    val LightEdge       = Color(0xFFE4E4DC)
}
