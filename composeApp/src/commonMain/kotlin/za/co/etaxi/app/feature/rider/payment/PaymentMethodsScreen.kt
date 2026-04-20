package za.co.etaxi.app.feature.rider.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.CardBrand
import za.co.etaxi.app.domain.model.PaymentMethod
import za.co.etaxi.app.domain.repository.PaymentRepository

object PaymentMethodsScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: PaymentRepository = koinInject()
        val scope = rememberCoroutineScope()

        var methods by remember { mutableStateOf<List<PaymentMethod>>(emptyList()) }
        var showAddSheet by remember { mutableStateOf(false) }

        suspend fun refresh() {
            methods = repo.methods()
        }
        LaunchedEffect(Unit) { refresh() }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 48.dp, bottom = 32.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Payment",
                        style = MaterialTheme.typography.headlineLarge,
                        color = EtaxiColors.Ink,
                    )
                }

                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    items(methods) { method ->
                        CardRow(
                            method = method,
                            onSetDefault = {
                                scope.launch {
                                    repo.setDefault(method.id)
                                    refresh()
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repo.removeCard(method.id)
                                    refresh()
                                }
                            },
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    1.5.dp,
                                    EtaxiColors.Power,
                                    RoundedCornerShape(18.dp),
                                )
                                .clickable { showAddSheet = true }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = EtaxiColors.Power)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Add a new card",
                                style = MaterialTheme.typography.titleMedium,
                                color = EtaxiColors.Power,
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Your cards are encrypted and handled by Stripe. eTaxi never sees your full card number.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.InkFaint,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showAddSheet,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                AddCardSheet(
                    onDismiss = { showAddSheet = false },
                    onAdded = {
                        showAddSheet = false
                        scope.launch { refresh() }
                    },
                )
            }
        }
    }
}

@Composable
private fun CardRow(
    method: PaymentMethod,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    val bgTint = when (method.brand) {
        CardBrand.VISA       -> EtaxiColors.Power
        CardBrand.MASTERCARD -> EtaxiColors.Warning
        CardBrand.AMEX       -> EtaxiColors.Success
        CardBrand.OTHER      -> EtaxiColors.InkMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgTint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.CreditCard, null, tint = bgTint)
        }

        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${method.brand.label} •• ${method.last4}",
                    style = MaterialTheme.typography.titleMedium,
                    color = EtaxiColors.Ink,
                )
                if (method.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EtaxiColors.PowerGlow)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "DEFAULT",
                            style = MaterialTheme.typography.labelSmall,
                            color = EtaxiColors.Power,
                        )
                    }
                }
            }
            Text(
                "Exp ${method.expiryMonth.toString().padStart(2, '0')}/${method.expiryYear % 100}",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkMuted,
            )
        }

        if (!method.isDefault) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSetDefault),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    tint = EtaxiColors.InkMuted,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Delete, null, tint = EtaxiColors.Danger)
        }
    }
}
