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
// A segmented toggle at the top picks the role; the rest of the form
// is identical. After submit, we call session.register(...) which hits
// POST /auth/register on the backend. Response is
// { requiresOtp, phone } — we navigate to OtpScreen passing phone.
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
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

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

                // Role toggle
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
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
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
                    onValueChange = { email = it },
                    label = "Email",
                    leading = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                )
                EtaxiTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone (+27...)",
                    leading = Icons.Rounded.Phone,
                    keyboardType = KeyboardType.Phone,
                )
                EtaxiTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
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
                    enabled = firstName.isNotBlank()
                        && lastName.isNotBlank()
                        && email.contains("@")
                        && phone.length >= 10
                        && password.length >= 8
                        && !loading,
                    onClick = {
                        loading = true
                        errorMessage = null
                        val normalizedPhone =
                            if (phone.startsWith("+")) phone else "+27${phone.trimStart('0')}"
                        scope.launch {
                            val result = session.register(
                                email = email.trim(),
                                phone = normalizedPhone,
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                role = role,
                            )
                            loading = false
                            if (result.isSuccess) {
                                navigator.push(OtpScreen(normalizedPhone))
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
        Spacer(Modifier.height(0.dp))
        Box(Modifier.padding(start = 8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = fg,
            )
        }
    }
}
