package za.co.etaxi.app.core.theme

import androidx.compose.ui.unit.dp

/**
 * eTaxi spacing scale — 4pt grid.
 *
 * Use these instead of hard-coded dp values. Naming follows the
 * t-shirt scale so it's immediately obvious which one you want.
 *
 * Do NOT add new sizes to this scale. If you find yourself reaching
 * for 13.dp or 28.dp, something is probably off — pick the nearest
 * token and adjust the surrounding rhythm.
 */
object EtaxiSpacing {
    val xxs  = 2.dp    // hairline separation
    val xs   = 4.dp    // tight gap between related bits (icon-text)
    val sm   = 8.dp    // small padding, row gap in lists
    val md   = 12.dp   // default gap between medium elements
    val lg   = 16.dp   // content padding, card interior
    val xl   = 20.dp   // card-to-card gap, section break
    val xxl  = 24.dp   // page padding left/right
    val xxxl = 32.dp   // vertical section separation
    val huge = 48.dp   // above-the-fold hero spacing
}
