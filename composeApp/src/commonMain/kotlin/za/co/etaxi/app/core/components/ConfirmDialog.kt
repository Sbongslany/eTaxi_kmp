package za.co.etaxi.app.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Simple confirm/cancel dialog themed to match the app.
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    confirmTone: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cancelLabel: String = "Cancel",
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(initialScale = 0.88f),
                exit = fadeOut() + scaleOut(targetScale = 0.88f),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(22.dp))
                        .padding(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = EtaxiColors.Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EtaxiColors.InkMuted,
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Cancel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(14.dp))
                                .clickable(onClick = onDismiss)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                cancelLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = EtaxiColors.Ink,
                                textAlign = TextAlign.Center,
                            )
                        }
                        // Confirm
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(confirmTone)
                                .clickable(onClick = onConfirm)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                confirmLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (confirmTone == EtaxiColors.Power)
                                    EtaxiColors.InkOnPower
                                else Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
