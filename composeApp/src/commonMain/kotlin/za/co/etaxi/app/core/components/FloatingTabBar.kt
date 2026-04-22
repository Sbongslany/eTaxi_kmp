package za.co.etaxi.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

data class TabItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * The floating pill tab bar from the reference screenshots.
 *
 * Unlike a traditional edge-to-edge bottom nav, this is a ROUNDED PILL
 * that floats above the bottom safe area, with a subtle border and a
 * pill-shape active indicator behind the current tab.
 *
 * Active tab: circled icon in the accent color, colored label.
 * Inactive tabs: grey icon, grey label.
 */
@Composable
fun FloatingTabBar(
    tabs: List<TabItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    accent: Color = EtaxiColors.Power,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(EtaxiColors.Ink700.copy(alpha = 0.92f))
            .border(1.dp, EtaxiColors.Ink500.copy(alpha = 0.6f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            TabCell(
                tab = tab,
                selected = tab.id == selectedId,
                accent = accent,
                onClick = { onSelect(tab.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabCell(
    tab: TabItem,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) accent else EtaxiColors.BoneMuted,
        label = "tint",
    )
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (selected) accent.copy(alpha = 0.14f) else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}
