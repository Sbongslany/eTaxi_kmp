package za.co.etaxi.app.feature.auth.register

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.otp.OtpScreen

// Unified register form for both riders and drivers.
//
// Fix vs previous version:
// - Phone must be exactly 9 digits of the subscriber number (no
//   leading 0, no country code). The "+27" is added automatically.
// - If the user types a leading "0" or "+27", we strip that so we
//   always end up with +27 + 9 digits = 12 characters total.
// - The final value sent to the backend is validated to have exactly
//   12 characters before submit is enabled.
object RegisterScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = remember { CoroutineScope(Dispatchers.Default) }

        var role by remember { mutableStateOf(UserRole.RIDER) }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phoneDigits by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Validation. SA subscriber numbers are 9 digits after +27.
        val phoneValid = phoneDigits.length == 9
        val emailValid = email.contains("@") && email.contains(".")
        val passwordValid = password.length >= 8
        val canSubmit = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                emailValid &&
                phoneValid &&
                passwordValid &&
                !loading

        Scaffold(
            containerColor = EtaxiColors.SlidesDeep,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null,
                                tint = EtaxiColors.Ink,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EtaxiColors.SlidesDeep
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Create account.",
                    style = MaterialTheme.typography.displayMedium,
                    color = EtaxiColors.Ink,
                )
                Text(
                    "Pick your role, then fill in your details.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.InkMuted,
                )

                Text(
                    "I AM A",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.InkFaint,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(EtaxiColors.Slides),
                ) {
                    RoleSegment(
                        label = "Rider",
                        icon = Icons.Rounded.PersonPin,
                        selected = role == UserRole.RIDER,
                        onClick = { role = UserRole.RIDER },
                        modifier = Modifier.weight(1f),
                    )
                    RoleSegment(
                        label = "Driver",
                        icon = Icons.Rounded.DirectionsCar,
                        selected = role == UserRole.DRIVER,
                        onClick = { role = UserRole.DRIVER },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EtaxiTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First name",
                        modifier = Modifier.weight(1f),
                        leading = Icons.Rounded.Person,
                    )
                    EtaxiTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last name",
                        modifier = Modifier.weight(1f),
                    )
                }
                EtaxiTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = "Email",
                    leading = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                )
                EtaxiTextField(
                    value = phoneDigits,
                    onValueChange = { raw ->
                        // Accept only digits. Strip a leading 0. Strip a
                        // leading "27" if user typed that by habit. Cap
                        // at 9 digits.
                        var d = raw.filter { it.isDigit() }
                        if (d.startsWith("27")) d = d.removePrefix("27")
                        if (d.startsWith("0")) d = d.removePrefix("0")
                        if (d.length > 9) d = d.take(9)
                        phoneDigits = d
                    },
                    label = "Phone (+27 prefix added automatically — 9 digits)",
                    leading = Icons.Rounded.Phone,
                    keyboardType = KeyboardType.Phone,
                )
                // Inline phone preview so the user sees what will be submitted.
                if (phoneDigits.isNotEmpty()) {
                    Text(
                        "Will submit as: +27$phoneDigits",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (phoneValid) EtaxiColors.Success
                                else EtaxiColors.InkFaint,
                    )
                }
                EtaxiTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password (min 8 characters)",
                    leading = Icons.Rounded.Lock,
                    isPassword = true,
                )

                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EtaxiColors.Danger,
                    )
                }

                Spacer(Modifier.height(8.dp))

                EtaxiPrimaryButton(
                    text = if (loading) "CREATING..." else "CONTINUE",
                    loading = loading,
                    enabled = canSubmit,
                    onClick = {
                        loading = true
                        errorMessage = null
                        val fullPhone = "+27$phoneDigits"
                        scope.launch {
                            val result = session.register(
                                email = email.trim().lowercase(),
                                phone = fullPhone,
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                role = role,
                            )
                            loading = false
                            if (result.isSuccess) {
                                navigator.push(OtpScreen(fullPhone))
                            } else {
                                errorMessage = result.exceptionOrNull()?.message
                                    ?: "Registration failed"
                            }
                        }
                    },
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RoleSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) EtaxiColors.Power else EtaxiColors.Slides
    val fg = if (selected) EtaxiColors.InkOnPower else EtaxiColors.InkMuted
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Box(Modifier.padding(start = 8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = fg,
            )
        }
    }
}
