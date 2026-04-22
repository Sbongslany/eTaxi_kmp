package za.co.etaxi.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * The big tappable service card from the "How do you want to get around?"
 * screen. An icon tile + title + badge + bullet list + forward arrow.
 *
 * @param accent color for both the border and the arrow. Use Power for
 *               Standard Ride, Hire for Custom Hire.
 * @param bullets 2-4 short feature strings shown with colored dots.
 */
@Composable
fun ServiceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String?,
    bullets: List<String>,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(EtaxiColors.Ink700)
            .border(1.5.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TintedIconTile(icon = icon, tint = accent, size = 52.dp, cornerRadius = 14.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
                        color = EtaxiColors.Bone,
                    )
                    badge?.let {
                        Spacer(Modifier.width(10.dp))
                        BadgePill(text = it, color = accent)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.BoneMuted,
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = if (accent == EtaxiColors.Power) EtaxiColors.PowerInk else EtaxiColors.Bone,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (bullets.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            bullets.forEach { bullet ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        bullet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EtaxiColors.BoneMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
