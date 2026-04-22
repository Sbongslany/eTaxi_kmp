package za.co.etaxi.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

enum class ServiceMode { STANDARD, HIRE }

/**
 * The two-state pill toggle used on History, Rider home, etc to switch
 * between Standard Ride and Custom Hire views. Follows reference screenshot
 * 6: ⚡ Standard | 📅 Custom Hire, selected segment fills with its accent.
 */
@Composable
fun ServiceModeToggle(
    mode: ServiceMode,
    onChange: (ServiceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(EtaxiColors.Ink700)
            .padding(4.dp),
    ) {
        Segment(
            label = "Standard",
            emoji = "\u26A1", // ⚡
            selected = mode == ServiceMode.STANDARD,
            selectedColor = EtaxiColors.Power,
            onSelectedFg = EtaxiColors.PowerInk,
            onClick = { onChange(ServiceMode.STANDARD) },
            modifier = Modifier.weight(1f),
        )
        Segment(
            label = "Custom Hire",
            emoji = "\uD83D\uDCC5", // 📅
            selected = mode == ServiceMode.HIRE,
            selectedColor = EtaxiColors.Hire,
            onSelectedFg = EtaxiColors.Bone,
            onClick = { onChange(ServiceMode.HIRE) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Segment(
    label: String,
    emoji: String,
    selected: Boolean,
    selectedColor: Color,
    onSelectedFg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        if (selected) selectedColor else Color.Transparent,
        label = "segbg",
    )
    val fg by animateColorAsState(
        if (selected) onSelectedFg else EtaxiColors.BoneMuted,
        label = "segfg",
    )
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = fg,
        )
    }
}
