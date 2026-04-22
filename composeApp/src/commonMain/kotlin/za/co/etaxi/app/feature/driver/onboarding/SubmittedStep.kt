package za.co.etaxi.app.feature.driver.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiSecondaryButton
import za.co.etaxi.app.core.theme.EtaxiColors

@Composable
internal fun SubmittedStep(onContinue: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "pop",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(EtaxiColors.Power),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Check,
                null,
                tint = EtaxiColors.InkOnPower,
                modifier = Modifier.size(80.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Submitted!",
            style = MaterialTheme.typography.displayMedium,
            color = EtaxiColors.Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your documents and vehicle are under review.",
            style = MaterialTheme.typography.bodyLarge,
            color = EtaxiColors.InkMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // What happens next
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            TimelineRow(
                icon = Icons.Rounded.Check,
                iconTint = EtaxiColors.Power,
                title = "Info submitted",
                subtitle = "Just now",
                completed = true,
            )
            Spacer(Modifier.height(16.dp))
            TimelineRow(
                icon = Icons.Rounded.HourglassTop,
                iconTint = EtaxiColors.Warning,
                title = "Under review",
                subtitle = "Usually takes 24–48 hours",
                completed = false,
            )
            Spacer(Modifier.height(16.dp))
            TimelineRow(
                icon = Icons.Rounded.Check,
                iconTint = EtaxiColors.InkFaint,
                title = "Start driving",
                subtitle = "You'll get a notification once approved",
                completed = false,
                dim = true,
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "You can still explore the driver app — earnings, trip history, and notifications are already active. The 'Go online' toggle will unlock once you're approved.",
            style = MaterialTheme.typography.bodySmall,
            color = EtaxiColors.InkFaint,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
        EtaxiPrimaryButton(
            text = "Explore the app",
            onClick = onContinue,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TimelineRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    completed: Boolean,
    dim: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (completed) iconTint.copy(alpha = 0.2f)
                    else EtaxiColors.SlidesRaised,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = if (dim) EtaxiColors.InkFaint else iconTint)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (dim) EtaxiColors.InkMuted else EtaxiColors.Ink,
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.InkFaint)
        }
    }
}
