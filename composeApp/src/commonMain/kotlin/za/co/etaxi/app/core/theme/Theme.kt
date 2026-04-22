package za.co.etaxi.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EtaxiDarkColors = darkColorScheme(
    primary              = EtaxiColors.Power,
    onPrimary            = EtaxiColors.PowerInk,
    primaryContainer     = EtaxiColors.PowerDim,
    onPrimaryContainer   = EtaxiColors.Power,

    secondary            = EtaxiColors.Hire,
    onSecondary          = EtaxiColors.Bone,
    secondaryContainer   = EtaxiColors.Ink600,
    onSecondaryContainer = EtaxiColors.Bone,

    tertiary             = EtaxiColors.Amber,
    onTertiary           = EtaxiColors.Ink900,
    tertiaryContainer    = EtaxiColors.Ink600,
    onTertiaryContainer  = EtaxiColors.BoneMuted,

    background           = EtaxiColors.Ink900,
    onBackground         = EtaxiColors.Bone,

    surface              = EtaxiColors.Ink700,
    onSurface            = EtaxiColors.Bone,
    surfaceVariant       = EtaxiColors.Ink600,
    onSurfaceVariant     = EtaxiColors.BoneMuted,

    surfaceTint          = EtaxiColors.Power,
    inverseSurface       = EtaxiColors.Bone,
    inverseOnSurface     = EtaxiColors.Ink900,
    inversePrimary       = EtaxiColors.PowerDim,

    outline              = EtaxiColors.Ink500,
    outlineVariant       = EtaxiColors.Ink400,

    error                = EtaxiColors.Flare,
    onError              = EtaxiColors.Bone,
    errorContainer       = EtaxiColors.DangerDim,
    onErrorContainer     = EtaxiColors.Flare,

    scrim                = androidx.compose.ui.graphics.Color.Black,
)

private val EtaxiLightColors = lightColorScheme(
    primary            = EtaxiColors.Power,
    onPrimary          = EtaxiColors.PowerInk,
    background         = EtaxiColors.LightBg,
    onBackground       = EtaxiColors.LightInk,
    surface            = EtaxiColors.LightSurface,
    onSurface          = EtaxiColors.LightInk,
    surfaceVariant     = EtaxiColors.LightBg,
    onSurfaceVariant   = EtaxiColors.LightInkMuted,
    outline            = EtaxiColors.LightEdge,
    outlineVariant     = EtaxiColors.LightEdge,
    error              = EtaxiColors.Flare,
)

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
