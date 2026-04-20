package za.co.etaxi.app.feature.auth.roleSelection

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.login.LoginScreen

object RoleSelectionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = remember { CoroutineScope(Dispatchers.Default) }

        var selected by remember { mutableStateOf<UserRole?>(null) }
        var entered by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { entered = true }

        Scaffold(containerColor = EtaxiColors.SlidesDeep) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AnimatedVisibility(
                    visible = entered,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(500),
                    ),
                ) {
                    Column {
                        Text(
                            text = "Move your way.",
                            style = MaterialTheme.typography.displayMedium,
                            color = EtaxiColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Choose how you want to use eTaxi.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = EtaxiColors.InkMuted,
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Spacer(Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = entered,
                        enter = slideInVertically(
                            initialOffsetY = { 120 },
                            animationSpec = tween(600, delayMillis = 100),
                        ) + fadeIn(tween(600, delayMillis = 100)),
                    ) {
                        RoleCard(
                            title = "I NEED A RIDE",
                            subtitle = "Get where you're going. Fast, safe, cashless.",
                            icon = Icons.Rounded.Person,
                            filled = true,
                            selected = selected == UserRole.RIDER,
                            onClick = { selected = UserRole.RIDER },
                        )
                    }
                    AnimatedVisibility(
                        visible = entered,
                        enter = slideInVertically(
                            initialOffsetY = { 140 },
                            animationSpec = tween(600, delayMillis = 200),
                        ) + fadeIn(tween(600, delayMillis = 200)),
                    ) {
                        RoleCard(
                            title = "I DRIVE",
                            subtitle = "Earn on your terms. Go online, get requests.",
                            icon = Icons.Rounded.DirectionsCar,
                            filled = false,
                            selected = selected == UserRole.DRIVER,
                            onClick = { selected = UserRole.DRIVER },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = selected != null,
                    enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { 40 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 }),
                ) {
                    EtaxiPrimaryButton(
                        text = "CONTINUE",
                        onClick = {
                            val role = selected ?: return@EtaxiPrimaryButton
                            scope.launch { session.pickRole(role) }
                            navigator.push(LoginScreen)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    filled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed || selected) 0.98f else 1f,
        animationSpec = spring(0.7f, 350f),
        label = "card",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> EtaxiColors.Power
            else -> EtaxiColors.SlidesEdge
        },
        animationSpec = tween(200),
        label = "border",
    )

    val bg = if (filled) EtaxiColors.Power else EtaxiColors.Slides
    val fg = if (filled) EtaxiColors.InkOnPower else EtaxiColors.Ink
    val subtle = if (filled) EtaxiColors.InkOnPower.copy(alpha = 0.75f) else EtaxiColors.InkMuted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .scale(scale)
            .clickable(interaction, indication = null, onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            // Icon in top-right
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (filled) EtaxiColors.InkOnPower.copy(alpha = 0.12f)
                                else EtaxiColors.Power.copy(alpha = 0.12f),
                        shape = CircleShape,
                    )
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (filled) EtaxiColors.InkOnPower else EtaxiColors.Power,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = fg,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtle,
                )
            }
        }
    }
}
