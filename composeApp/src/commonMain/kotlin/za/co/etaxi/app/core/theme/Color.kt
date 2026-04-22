package za.co.etaxi.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * eTaxi brand palette — reconciled version.
 *
 * This file contains TWO naming systems that coexist:
 *
 *  1) The ORIGINAL names used throughout the existing codebase
 *     (Ink / InkMuted / InkFaint / InkOnPower, Slides / SlidesDeep /
 *     SlidesRaised / SlidesEdge / SlidesSoft, Success / Warning / Danger,
 *     PowerMuted).
 *
 *  2) The NEW names introduced in the Patch 7b visual refresh
 *     (Bone / BoneMuted / BoneFaint, Ink900/800/700/600/500/400,
 *     Hire / HireDim / HireGlow, Amber / Cyan / Plum / Ruby / Flare / Fern).
 *
 * Both point to the same underlying colors so every screen compiles
 * regardless of which names it references. Do not remove any token from
 * this file without auditing the codebase for references first.
 *
 * Anchors:
 *   POWER  = #C3D809 (lime) — primary brand, Standard Ride, CTAs
 *   HIRE   = #6C63FF (indigo) — Custom Hire ONLY
 *   SLIDES = #222022 (near-black) — primary surface
 */
object EtaxiColors {

    // ============================================================
    // ORIGINAL SYSTEM (used by the existing codebase)
    // ============================================================

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
    val Ink          = Color(0xFFF5F5F0)   // primary text (slight warm white)
    val InkMuted     = Color(0xFFB6B6B0)   // secondary text
    val InkFaint     = Color(0xFF7A7A75)   // tertiary / helper text
    val InkOnPower   = Color(0xFF0A0A0A)   // text sitting on lime (near-black)

    // ===== Semantic =====
    val Success      = Color(0xFF3DD68C)
    val Warning      = Color(0xFFFFB547)
    val Danger       = Color(0xFFFF5757)
    val DangerDim    = Color(0xFF2A1416)

    // ===== Light mode (fallback if user enables light) =====
    val LightBg          = Color(0xFFF5F5F0)
    val LightSurface     = Color(0xFFFFFFFF)
    val LightInk         = Color(0xFF0A0A0A)
    val LightInkMuted    = Color(0xFF5F5F5A)
    val LightEdge        = Color(0xFFE4E4DC)

    // ============================================================
    // SECONDARY BRAND — Custom Hire ONLY (new in 7b, stays)
    // ============================================================

    val Hire         = Color(0xFF6C63FF)   // indigo — Custom Hire
    val HireDim      = Color(0xFF4F47D6)   // hovered/pressed indigo
    val HireGlow     = Color(0x336C63FF)   // 20% indigo for glows & chips

    // ============================================================
    // NEW NAMES FROM PATCH 7b — aliased to original system so every
    // screen in this repo compiles without modification. Prefer the
    // ORIGINAL names above in new code; these exist only to keep the
    // 7b-era screens working.
    // ============================================================

    // Bone = Ink (warm-white text ramp)
    val Bone         = Ink
    val BoneMuted    = InkMuted
    val BoneFaint    = InkFaint
    val PowerInk     = InkOnPower

    // Ink ramp from 7b maps to the Slides ramp
    val Ink900       = SlidesDeep
    val Ink800       = Slides
    val Ink700       = SlidesRaised
    val Ink600       = Color(0xFF323135)   // between Raised and Edge
    val Ink500       = SlidesEdge
    val Ink400       = SlidesSoft

    // Categorical accents from 7b — used by icon tiles, profile rows, etc.
    val Amber        = Warning              // yellow/amber — warnings, in-progress
    val Cyan         = Color(0xFF4DD0E1)    // info blue
    val Info         = Cyan
    val Plum         = Color(0xFFB886FF)    // promos, rewards
    val Ruby         = Danger               // safety/emergency
    val Fern         = Success              // positive-but-not-brand
    val Flare        = Danger               // destructive actions

    // Legacy Material-ish aliases some components still reference
    val AccentPrimary    = Power
    val AccentSecondary  = Hire
    val Background       = SlidesDeep
    val Surface          = Slides
    val SurfaceVariant   = SlidesRaised
    val OnSurface        = Ink
    val OnSurfaceVariant = InkMuted
    val Outline          = SlidesEdge
    val OutlineVariant   = SlidesSoft
    val Error            = Danger
}
