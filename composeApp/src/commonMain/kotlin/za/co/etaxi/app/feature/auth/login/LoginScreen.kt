package za.co.etaxi.app.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.data.remote.PhoneVerificationRequiredException
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.otp.OtpScreen
import za.co.etaxi.app.feature.auth.register.RegisterScreen

// Login screen. Accepts either email OR phone as the identifier.
// Normalization rules:
//   - If input contains "@", treat as email, pass through lowercased.
//   - Otherwise treat as phone:
//       * If starts with "+", pass through unchanged.
//       * If starts with "0", strip the 0 and prepend "+27".
//       * If starts with "27", prepend "+".
//       * Otherwise prepend "+27".
//   - Validate result has at least 10 chars of phone OR a valid email shape.
object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = rememberCoroutineScope()

        var identifier by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.SlidesDeep)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 32.dp),
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EtaxiColors.Power),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "eT",
                        style = MaterialTheme.typography.titleLarge,
                        color = EtaxiColors.InkOnPower,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    "eTaxi",
                    style = MaterialTheme.typography.displaySmall,
                    color = EtaxiColors.Power,
                )
            }

            Spacer(Modifier.height(56.dp))

            Text(
                "Welcome back \uD83D\uDC4B",
                style = MaterialTheme.typography.displaySmall,
                color = EtaxiColors.Ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Sign in with your email or phone number",
                style = MaterialTheme.typography.bodyLarge,
                color = EtaxiColors.InkMuted,
            )

            Spacer(Modifier.height(40.dp))

            Text(
                "EMAIL OR PHONE",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = identifier,
                onValueChange = { identifier = it.trim() },
                placeholder = {
                    Text(
                        "you@example.com  or  +27821234567",
                        color = EtaxiColors.InkFaint,
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Rounded.AlternateEmail,
                        contentDescription = null,
                        tint = EtaxiColors.InkMuted,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = transparentTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EtaxiColors.Slides),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "PASSWORD",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "Enter your password",
                        color = EtaxiColors.InkFaint,
                    )
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Icon(
                        if (showPassword) Icons.Rounded.VisibilityOff
                        else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = EtaxiColors.InkMuted,
                        modifier = Modifier.clickable { showPassword = !showPassword },
                    )
                },
                colors = transparentTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EtaxiColors.Slides),
            )

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.Danger,
                )
            }

            Spacer(Modifier.height(24.dp))

            EtaxiPrimaryButton(
                text = if (loading) "SIGNING IN..." else "Sign in",
                loading = loading,
                enabled = identifier.isNotBlank() && password.isNotBlank() && !loading,
                onClick = {
                    loading = true
                    errorMessage = null
                    val normalized = normalizeIdentifier(identifier)
                    scope.launch {
                        val result = session.login(normalized, password)
                        loading = false
                        when {
                            result.isSuccess -> {
                                nav.replaceAll(HomeScreen)
                            }
                            result.exceptionOrNull() is PhoneVerificationRequiredException -> {
                                nav.push(OtpScreen(normalized))
                            }
                            else -> {
                                errorMessage = result.exceptionOrNull()?.message
                                    ?: "Could not sign in. Check your details and try again."
                            }
                        }
                    }
                },
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(EtaxiColors.SlidesEdge),
                )
                Text(
                    "OR",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.InkFaint,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(EtaxiColors.SlidesEdge),
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EtaxiColors.Slides)
                    .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
                    .clickable { nav.push(RegisterScreen) }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Create an account",
                    style = MaterialTheme.typography.titleMedium,
                    color = EtaxiColors.Ink,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Works for both passengers and drivers",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

// Normalize what the user typed into either an email or a canonical
// SA phone number. The backend's login endpoint accepts both.
//
// Rules:
//   - Contains "@"  -> lowercase email, pass through
//   - Starts "+"    -> already in international format, strip spaces
//   - Starts "0"    -> drop the 0, prepend "+27" (local SA format)
//   - Starts "27"   -> prepend "+"
//   - Otherwise     -> prepend "+27" (assume just the subscriber digits)
private fun normalizeIdentifier(input: String): String {
    val trimmed = input.trim()
    if (trimmed.contains("@")) return trimmed.lowercase()
    val digitsPlus = trimmed.filter { it.isDigit() || it == '+' }
    return when {
        digitsPlus.startsWith("+") -> digitsPlus
        digitsPlus.startsWith("0") -> "+27" + digitsPlus.drop(1)
        digitsPlus.startsWith("27") -> "+$digitsPlus"
        else -> "+27$digitsPlus"
    }
}

@Composable
private fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedTextColor = EtaxiColors.Ink,
    unfocusedTextColor = EtaxiColors.Ink,
    cursorColor = EtaxiColors.Power,
)
