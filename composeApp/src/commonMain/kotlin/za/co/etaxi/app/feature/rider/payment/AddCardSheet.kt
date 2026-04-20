package za.co.etaxi.app.feature.rider.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.repository.PaymentRepository

@Composable
fun AddCardSheet(
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
) {
    val repo: PaymentRepository = koinInject()
    val scope = rememberCoroutineScope()

    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") } // MM/YY
    var cvc by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val last4 = cardNumber.filter { it.isDigit() }.takeLast(4)
    val brand = when {
        cardNumber.startsWith("4") -> "Visa"
        cardNumber.startsWith("5") -> "Mastercard"
        cardNumber.startsWith("3") -> "Amex"
        else -> "Card"
    }
    val (mm, yy) = expiry.split("/").let { parts ->
        val m = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        val y = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        m to y
    }
    val canSave = last4.length == 4 &&
        mm in 1..12 &&
        yy in 25..99 &&
        cvc.length in 3..4 &&
        !saving

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 48.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.SlidesSoft),
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Add card",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EtaxiColors.Ink,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.SlidesRaised)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Close, null, tint = EtaxiColors.Ink)
                }
            }

            Spacer(Modifier.height(20.dp))

            EtaxiTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 19) cardNumber = it.filter { c -> c.isDigit() || c == ' ' } },
                label = "Card number",
                placeholder = "1234 5678 9012 3456",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(
                        value = expiry,
                        onValueChange = {
                            val cleaned = it.filter { c -> c.isDigit() || c == '/' }
                            if (cleaned.length <= 5) expiry = cleaned
                        },
                        label = "Expiry",
                        placeholder = "MM/YY",
                    )
                }
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(
                        value = cvc,
                        onValueChange = { if (it.length <= 4) cvc = it.filter { c -> c.isDigit() } },
                        label = "CVC",
                        placeholder = "123",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Test card: 4242 4242 4242 4242 • any future expiry • any CVC",
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkFaint,
            )

            Spacer(Modifier.height(24.dp))
            EtaxiPrimaryButton(
                text = "Save card",
                enabled = canSave,
                loading = saving,
                onClick = {
                    scope.launch {
                        saving = true
                        repo.addCard(brand, last4, mm, 2000 + yy)
                        saving = false
                        onAdded()
                    }
                },
            )
        }
    }
}
