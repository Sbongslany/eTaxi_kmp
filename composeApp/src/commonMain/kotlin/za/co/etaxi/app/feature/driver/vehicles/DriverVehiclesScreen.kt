package za.co.etaxi.app.feature.driver.vehicles

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
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.VehicleStatus
import za.co.etaxi.app.domain.repository.DriverRepository

object DriverVehiclesScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val scope = rememberCoroutineScope()

        var vehicles by remember { mutableStateOf<List<DriverVehicle>>(emptyList()) }
        var showAddSheet by remember { mutableStateOf(false) }

        suspend fun refresh() { vehicles = repo.vehicles() }
        LaunchedEffect(Unit) { refresh() }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize().padding(top = 48.dp, bottom = 32.dp)) {
                Row(
                    Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { nav.pop() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.ArrowBack, "back", tint = EtaxiColors.Ink)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Vehicles", style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.Ink)
                        Text(
                            "Tap a vehicle to make it active",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.InkMuted,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(vehicles) { v ->
                        VehicleCard(
                            vehicle = v,
                            onSetActive = {
                                if (v.status == VehicleStatus.APPROVED && !v.isActive) {
                                    scope.launch {
                                        repo.setActiveVehicle(v.id)
                                        refresh()
                                    }
                                }
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, EtaxiColors.Power, RoundedCornerShape(18.dp))
                                .clickable { showAddSheet = true }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = EtaxiColors.Power)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Register a new vehicle",
                                style = MaterialTheme.typography.titleMedium,
                                color = EtaxiColors.Power,
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "New vehicles require approval before you can go online with them. Approvals usually happen within 24 hours.",
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
                AddVehicleSheet(
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
private fun VehicleCard(vehicle: DriverVehicle, onSetActive: () -> Unit) {
    val clickable = vehicle.status == VehicleStatus.APPROVED && !vehicle.isActive
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.5.dp,
                if (vehicle.isActive) EtaxiColors.Power else EtaxiColors.SlidesEdge,
                RoundedCornerShape(20.dp),
            )
            .then(if (clickable) Modifier.clickable(onClick = onSetActive) else Modifier)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EtaxiColors.PowerGlow),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.DirectionsCar, null, tint = EtaxiColors.Power)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(vehicle.displayName, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${vehicle.color} · ${vehicle.plateNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EtaxiColors.InkMuted,
                )
            }
            if (vehicle.isActive) {
                Icon(Icons.Rounded.CheckCircle, null, tint = EtaxiColors.Power)
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(vehicle.status)
            Spacer(Modifier.width(8.dp))
            Chip(vehicle.vehicleClass.label, EtaxiColors.SlidesRaised, EtaxiColors.InkMuted)
            if (vehicle.isActive) {
                Spacer(Modifier.width(8.dp))
                Chip("ACTIVE", EtaxiColors.Power, EtaxiColors.InkOnPower)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: VehicleStatus) {
    val (label, bg, fg) = when (status) {
        VehicleStatus.APPROVED    -> Triple("APPROVED", EtaxiColors.Success.copy(alpha = 0.18f), EtaxiColors.Success)
        VehicleStatus.PENDING     -> Triple("PENDING",  EtaxiColors.Warning.copy(alpha = 0.18f), EtaxiColors.Warning)
        VehicleStatus.REJECTED    -> Triple("REJECTED", EtaxiColors.Danger.copy(alpha = 0.18f),  EtaxiColors.Danger)
        VehicleStatus.DEACTIVATED -> Triple("INACTIVE", EtaxiColors.SlidesRaised,                EtaxiColors.InkMuted)
    }
    Chip(label, bg, fg)
}

@Composable
private fun Chip(label: String, bg: Color, fg: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
