package za.co.etaxi.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * eTaxi typography v3 — matches the reference screenshots.
 *
 * Hero moments ("Scheduled", "1d 3h 58m", "Welcome back") use 68-80sp
 * with heavy weight and negative tracking. This is where the app feels
 * premium — one big type moment per screen.
 *
 * Mid scales (28-22sp) for screen titles like "My Trips", "Wallet".
 * Body stays calm at 14-16sp.
 * Micro uppercase labels at 11-12sp with tight tracking for metadata.
 */
private val Display = FontFamily.SansSerif
private val Body    = FontFamily.SansSerif

val EtaxiTypography = Typography(
    // ====== DISPLAY — hero headlines ======
    displayLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Black,
        fontSize = 80.sp,
        lineHeight = 80.sp,
        letterSpacing = (-3).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-2).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1).sp,
    ),

    // ====== HEADLINE — screen titles ======
    headlineLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),

    // ====== TITLE — cards, rows ======
    titleLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),

    // ====== BODY ======
    bodyLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),

    // ====== LABEL ======
    labelLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Uppercase metadata like "START", "DURATION", "TOTAL FARE"
    labelMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
)

val ButtonTextStyle = TextStyle(
    fontFamily = Body,
    fontWeight = FontWeight.Bold,
    fontSize = 17.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)
