package za.co.etaxi.app.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.ButtonTextStyle
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Primary CTA button — lime on dark, 60dp tall, 20dp corner radius.
 * Press: 0.96 scale with a bouncy spring (noticeably tactile).
 * Loading: a pulsing dot replaces the label.
 * Optional trailing/leading icon.
 */
@Composable
fun EtaxiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "press",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            ),
        color = if (enabled) EtaxiColors.Power else EtaxiColors.Ink500,
        contentColor = EtaxiColors.PowerInk,
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                PulsingDot(color = EtaxiColors.PowerInk)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    leadingIcon?.let {
                        Icon(it, null, tint = EtaxiColors.PowerInk, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = text,
                        style = ButtonTextStyle,
                        color = if (enabled) EtaxiColors.PowerInk else EtaxiColors.BoneFaint,
                    )
                    trailingIcon?.let {
                        Icon(it, null, tint = EtaxiColors.PowerInk, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Secondary CTA — lime-outlined pill on dark.
 */
@Composable
fun EtaxiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(0.55f, 500f),
        label = "press",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clickable(interaction, indication = null, enabled = enabled, onClick = onClick),
        color = Color.Transparent,
        contentColor = EtaxiColors.Bone,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, EtaxiColors.Power),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                leadingIcon?.let {
                    Icon(it, null, tint = EtaxiColors.Bone, modifier = Modifier.size(18.dp))
                }
                Text(text, style = ButtonTextStyle, color = EtaxiColors.Bone)
            }
        }
    }
}

/**
 * Ghost button — no fill, no border. For tertiary actions where we
 * explicitly don't want competition with the primary CTA.
 * e.g. "Skip", "Not now".
 */
@Composable
fun EtaxiGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(interaction, indication = null, enabled = enabled, onClick = onClick),
        color = Color.Transparent,
        contentColor = EtaxiColors.BoneMuted,
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = ButtonTextStyle, color = EtaxiColors.BoneMuted)
        }
    }
}
