package za.co.etaxi.app.feature.driver.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.repository.DriverRepository

@Composable
fun AddVehicleSheet(
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
) {
    val repo: DriverRepository = koinInject()
    val scope = rememberCoroutineScope()

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var vehicleClass by remember { mutableStateOf(VehicleClass.STANDARD) }
    var saving by remember { mutableStateOf(false) }

    val yearInt = year.toIntOrNull() ?: 0
    val canSave = make.isNotBlank() && model.isNotBlank() &&
        yearInt in 2005..2027 && color.isNotBlank() &&
        plate.isNotBlank() && !saving

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
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 48.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.SlidesSoft),
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Register a vehicle",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EtaxiColors.Ink,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(value = make, onValueChange = { make = it }, label = "Make", placeholder = "Toyota")
                }
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(value = model, onValueChange = { model = it }, label = "Model", placeholder = "Corolla Quest")
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4) year = it.filter { c -> c.isDigit() } },
                        label = "Year",
                        placeholder = "2021",
                        keyboardType = KeyboardType.Number,
                    )
                }
                Box(Modifier.weight(1f)) {
                    EtaxiTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = "Colour",
                        placeholder = "Silver",
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            EtaxiTextField(
                value = plate,
                onValueChange = { plate = it.uppercase() },
                label = "Plate number",
                placeholder = "GP 123 456",
            )

            Spacer(Modifier.height(20.dp))
            Text("VEHICLE CLASS", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VehicleClass.entries.forEach { cls ->
                    val selected = cls == vehicleClass
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) EtaxiColors.PowerGlow else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (selected) EtaxiColors.Power else EtaxiColors.SlidesEdge,
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { vehicleClass = cls }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            cls.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) EtaxiColors.Power else EtaxiColors.InkMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            EtaxiPrimaryButton(
                text = "Submit for approval",
                enabled = canSave,
                loading = saving,
                onClick = {
                    scope.launch {
                        saving = true
                        repo.addVehicle(make, model, yearInt, color, plate, vehicleClass)
                        saving = false
                        onAdded()
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Your vehicle will go into PENDING status. You'll be notified once approved.",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
