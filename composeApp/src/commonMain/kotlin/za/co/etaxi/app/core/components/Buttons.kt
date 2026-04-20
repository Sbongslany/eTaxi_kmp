package za.co.etaxi.app.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.etaxi.app.core.theme.EtaxiColors

private val ButtonTextStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.2.sp,
)

@Composable
fun EtaxiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "press",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            ),
        color = if (enabled) EtaxiColors.Power else EtaxiColors.PowerMuted,
        contentColor = EtaxiColors.InkOnPower,
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                PulsingDot(color = EtaxiColors.InkOnPower)
            } else {
                Text(
                    text = text,
                    style = ButtonTextStyle,
                    color = EtaxiColors.InkOnPower,
                )
            }
        }
    }
}

@Composable
fun EtaxiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(0.6f, 400f),
        label = "press",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clickable(interaction, indication = null, enabled = enabled, onClick = onClick),
        color = Color.Transparent,
        contentColor = EtaxiColors.Ink,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, EtaxiColors.Power),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                style = ButtonTextStyle,
                color = EtaxiColors.Ink,
            )
        }
    }
}
