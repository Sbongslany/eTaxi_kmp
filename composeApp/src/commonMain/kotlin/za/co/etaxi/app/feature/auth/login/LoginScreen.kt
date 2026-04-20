package za.co.etaxi.app.feature.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiSecondaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.otp.OtpScreen
import za.co.etaxi.app.feature.auth.register.RegisterScreen

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = rememberCoroutineScope()

        var identifier by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var loading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { nav.pop() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.ArrowBack, "back", tint = EtaxiColors.Ink)
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "Welcome\nback.",
                style = MaterialTheme.typography.displayLarge,
                color = EtaxiColors.Ink,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Log in to keep moving.",
                style = MaterialTheme.typography.bodyLarge,
                color = EtaxiColors.InkMuted,
            )

            Spacer(Modifier.height(40.dp))

            EtaxiTextField(
                value = identifier,
                onValueChange = { identifier = it; error = null },
                label = "Email or phone",
                placeholder = "you@example.com",
            )
            Spacer(Modifier.height(16.dp))
            EtaxiTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = "Password",
                placeholder = "••••••••",
                isPassword = true,
            )

            AnimatedVisibility(error != null, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.Danger,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Forgot password?",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.Power,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.align(Alignment.End).clickable { },
            )

            Spacer(Modifier.height(32.dp))

            EtaxiPrimaryButton(
                text = "Log in",
                loading = loading,
                enabled = identifier.isNotBlank() && password.isNotBlank() && !loading,
                onClick = {
                    scope.launch {
                        loading = true
                        val result = session.login(identifier.trim(), password)
                        loading = false
                        result
                            .onSuccess { nav.replaceAll(HomeScreen) }
                            .onFailure { error = it.message ?: "Could not log in" }
                    }
                },
            )
            Spacer(Modifier.height(16.dp))
            EtaxiSecondaryButton(
                text = "Continue with OTP",
                onClick = { nav.push(OtpScreen(identifier.ifBlank { "+27000000000" })) },
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "New here? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.InkMuted,
                )
                Text(
                    "Create an account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.Power,
                    modifier = Modifier.clickable { nav.push(RegisterScreen) },
                )
            }
        }
    }
}
