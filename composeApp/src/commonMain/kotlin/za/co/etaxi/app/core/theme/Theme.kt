package za.co.etaxi.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EtaxiDarkColors = darkColorScheme(
    primary            = EtaxiColors.Power,
    onPrimary          = EtaxiColors.InkOnPower,
    primaryContainer   = EtaxiColors.PowerMuted,
    onPrimaryContainer = EtaxiColors.Power,

    secondary          = EtaxiColors.Power,
    onSecondary        = EtaxiColors.InkOnPower,
    secondaryContainer = EtaxiColors.SlidesRaised,
    onSecondaryContainer = EtaxiColors.Ink,

    tertiary           = EtaxiColors.Power,
    onTertiary         = EtaxiColors.InkOnPower,
    tertiaryContainer  = EtaxiColors.SlidesRaised,
    onTertiaryContainer = EtaxiColors.InkMuted,

    background         = EtaxiColors.SlidesDeep,
    onBackground       = EtaxiColors.Ink,

    surface            = EtaxiColors.Slides,
    onSurface          = EtaxiColors.Ink,
    surfaceVariant     = EtaxiColors.SlidesRaised,
    onSurfaceVariant   = EtaxiColors.InkMuted,

    surfaceTint        = EtaxiColors.Power,
    inverseSurface     = EtaxiColors.Ink,
    inverseOnSurface   = EtaxiColors.SlidesDeep,
    inversePrimary     = EtaxiColors.PowerMuted,

    outline            = EtaxiColors.SlidesEdge,
    outlineVariant     = EtaxiColors.SlidesSoft,

    error              = EtaxiColors.Danger,
    onError            = EtaxiColors.Ink,
    errorContainer     = EtaxiColors.DangerDim,
    onErrorContainer   = EtaxiColors.Danger,

    scrim              = androidx.compose.ui.graphics.Color.Black,
)

private val EtaxiLightColors = lightColorScheme(
    primary            = EtaxiColors.Power,
    onPrimary          = EtaxiColors.InkOnPower,
    primaryContainer   = EtaxiColors.Power,
    onPrimaryContainer = EtaxiColors.InkOnPower,

    background         = EtaxiColors.LightBg,
    onBackground       = EtaxiColors.LightInk,
    surface            = EtaxiColors.LightSurface,
    onSurface          = EtaxiColors.LightInk,
    surfaceVariant     = EtaxiColors.LightBg,
    onSurfaceVariant   = EtaxiColors.LightInkMuted,

    outline            = EtaxiColors.LightEdge,
    outlineVariant     = EtaxiColors.LightEdge,

    error              = EtaxiColors.Danger,
)

/**
 * Root theme. Defaults to dark mode (matches the brand reference).
 * `useDarkTheme = false` switches to a minimal light scheme if needed.
 */
@Composable
fun EtaxiTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) EtaxiDarkColors else EtaxiLightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = EtaxiTypography,
        shapes      = EtaxiShapes,
        content     = content,
    )
}
