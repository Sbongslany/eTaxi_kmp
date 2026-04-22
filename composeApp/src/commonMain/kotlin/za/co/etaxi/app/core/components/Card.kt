package za.co.etaxi.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.theme.EtaxiSpacing

/**
 * Card variants:
 *   • Hero    — the focal point of a screen. Lime accent border, deeper fill.
 *   • Raised  — standard elevated content. Subtle border, default surface.
 *   • Subtle  — background tile, barely delineated from the page.
 *
 * Use ONE Hero per screen. Multiple Raised are fine. Subtle for grouped lists.
 */
@Composable
fun EtaxiHeroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(EtaxiColors.Ink700)
        .border(1.5.dp, EtaxiColors.Power.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
    Box(
        modifier = modifier
            .then(base)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(EtaxiSpacing.xl),
    ) {
        content()
    }
}

@Composable
fun EtaxiRaisedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,       // optional colored border (e.g. warning)
    content: @Composable () -> Unit,
) {
    val base = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(EtaxiColors.Ink700)
        .border(
            1.dp,
            accent ?: EtaxiColors.Ink500.copy(alpha = 0.6f),
            RoundedCornerShape(18.dp),
        )
    Box(
        modifier = modifier
            .then(base)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(EtaxiSpacing.lg),
    ) {
        content()
    }
}

@Composable
fun EtaxiSubtleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(EtaxiColors.Ink800)
    Box(
        modifier = modifier
            .then(base)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(EtaxiSpacing.lg),
    ) {
        content()
    }
}
