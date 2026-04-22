package za.co.etaxi.app.feature.auth.otp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.SessionRepository

data class OtpScreen(val phone: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val driverRepo: DriverRepository = koinInject()
        val scope = remember { CoroutineScope(Dispatchers.Default) }

        var code by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Scaffold(
            containerColor = EtaxiColors.SlidesDeep,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = EtaxiColors.Ink)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = EtaxiColors.SlidesDeep),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text("Verify your number.", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)
                Text(
                    "We sent a 6-digit code to $phone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.InkMuted,
                )

                // Invisible input that drives the six boxes
                Box(contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) code = it },
                        textStyle = TextStyle(fontSize = 1.sp),  // effectively invisible
                        modifier = Modifier.focusRequester(focusRequester).alpha(0.01f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        cursorBrush = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(6) { i ->
                            val filled = code.length > i
                            val active = code.length == i
                            OtpBox(
                                char = code.getOrNull(i)?.toString() ?: "",
                                filled = filled,
                                active = active,
                            )
                        }
                    }
                }

                Text(
                    "Resend code",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.Power,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.weight(1f))

                EtaxiPrimaryButton(
                    text = if (loading) "VERIFYING…" else "VERIFY",
                    loading = loading,
                    enabled = code.length == 6,
                    onClick = {
                        loading = true
                        scope.launch {
                            session.verifyOtp(code)
                            val role = session.selectedRole.first() ?: UserRole.RIDER
                            // Brand-new driver: wipe mock fixtures so they start with
                            // zero vehicles and missing docs, triggering onboarding.
                            if (role == UserRole.DRIVER) {
                                driverRepo.resetForNewDriver()
                            }
                            loading = false
                            navigator.replaceAll(HomeScreen)
                        }
                    },
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OtpBox(char: String, filled: Boolean, active: Boolean) {
    val borderColor by animateColorAsState(
        targetValue = when {
            active -> EtaxiColors.Power
            filled -> EtaxiColors.PowerMuted
            else -> EtaxiColors.SlidesEdge
        },
        label = "border",
    )
    val bg = if (filled) EtaxiColors.Slides else EtaxiColors.SlidesDeep
    Box(
        modifier = Modifier
            .size(48.dp, 56.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = EtaxiColors.Ink,
            textAlign = TextAlign.Center,
        )
    }
}
