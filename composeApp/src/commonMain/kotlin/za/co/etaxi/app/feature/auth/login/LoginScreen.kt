package za.co.etaxi.app.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
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
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.register.RegisterScreen

/**
 * Login — reference screenshot 2.
 * Big "Welcome back 👋" display, SA flag + country prefix phone input,
 * password with eye toggle, lime primary Sign In, "OR" divider,
 * outlined Create account button.
 */
object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = rememberCoroutineScope()

        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 32.dp),
        ) {
            // Logo row: lime eT tile + wordmark
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
                        color = EtaxiColors.PowerInk,
                    )
                }
                Spacer(Modifier.width(12.dp))
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
                color = EtaxiColors.Bone,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = EtaxiColors.BoneMuted,
            )

            Spacer(Modifier.height(40.dp))

            Text("PHONE NUMBER", style = MaterialTheme.typography.labelMedium, color = EtaxiColors.BoneFaint)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EtaxiColors.Ink700),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("\uD83C\uDDFF\uD83C\uDDE6", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "+27",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.Bone,
                    )
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(EtaxiColors.Ink500),
                )
                TextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("82 456 7890", color = EtaxiColors.BoneFaint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = transparentTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text("PASSWORD", style = MaterialTheme.typography.labelMedium, color = EtaxiColors.BoneFaint)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password", color = EtaxiColors.BoneFaint) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Icon(
                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = EtaxiColors.BoneMuted,
                        modifier = Modifier.clickable { showPassword = !showPassword },
                    )
                },
                colors = transparentTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EtaxiColors.Ink700),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Forgot password?",
                style = MaterialTheme.typography.titleSmall,
                color = EtaxiColors.Power,
                modifier = Modifier.align(Alignment.End).clickable { /* TODO */ }.padding(4.dp),
            )

            Spacer(Modifier.height(24.dp))

            EtaxiPrimaryButton(
                text = "Sign in",
                loading = loading,
                enabled = phone.isNotBlank() && password.isNotBlank(),
                onClick = {
                    loading = true
                    scope.launch {
                        session.login("+27$phone", password)
                        loading = false
                        nav.replaceAll(HomeScreen)
                    }
                },
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(EtaxiColors.Ink500))
                Text(
                    "OR",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.BoneFaint,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Box(Modifier.weight(1f).height(1.dp).background(EtaxiColors.Ink500))
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EtaxiColors.Ink700)
                    .border(1.dp, EtaxiColors.Ink500, RoundedCornerShape(20.dp))
                    .clickable { nav.push(RegisterScreen) }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Create an account",
                    style = MaterialTheme.typography.titleMedium,
                    color = EtaxiColors.Bone,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Works for both passengers and drivers",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.BoneFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
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
    focusedTextColor = EtaxiColors.Bone,
    unfocusedTextColor = EtaxiColors.Bone,
    cursorColor = EtaxiColors.Power,
)
