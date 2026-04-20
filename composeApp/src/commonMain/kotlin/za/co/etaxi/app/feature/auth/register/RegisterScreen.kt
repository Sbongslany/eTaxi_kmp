package za.co.etaxi.app.feature.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.otp.OtpScreen

object RegisterScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = remember { CoroutineScope(Dispatchers.Default) }

        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Create account.", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)
                Text(
                    "We'll text you a code to verify your number.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.InkMuted,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EtaxiTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First name",
                        modifier = Modifier.weight(1f),
                        leading = Icons.Rounded.Person
                    )
                    EtaxiTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last name",
                        modifier = Modifier.weight(1f)
                    )
                }
                EtaxiTextField(email, { email = it }, "Email", leading = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
                EtaxiTextField(phone, { phone = it }, "Phone (+27…)", leading = Icons.Rounded.Phone, keyboardType = KeyboardType.Phone)
                EtaxiTextField(password, { password = it }, "Password", leading = Icons.Rounded.Lock, isPassword = true)

                Spacer(Modifier.weight(1f))

                EtaxiPrimaryButton(
                    text = if (loading) "CREATING…" else "CONTINUE",
                    loading = loading,
                    enabled = firstName.isNotBlank() && lastName.isNotBlank()
                        && email.contains("@") && phone.length >= 10 && password.length >= 6,
                    onClick = {
                        loading = true
                        scope.launch {
                            val role = session.selectedRole.first() ?: UserRole.RIDER
                            session.register(email, phone, password, firstName, lastName, role)
                            loading = false
                            navigator.push(OtpScreen(phone))
                        }
                    },
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
