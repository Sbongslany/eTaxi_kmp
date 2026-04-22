package za.co.etaxi.app.feature.driver.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
internal fun VehicleStep(onComplete: () -> Unit) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Your vehicle",
            style = MaterialTheme.typography.displaySmall,
            color = EtaxiColors.Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tell us about the car you'll be driving.",
            style = MaterialTheme.typography.bodyMedium,
            color = EtaxiColors.InkMuted,
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                EtaxiTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = "Make",
                    placeholder = "Toyota",
                )
            }
            Box(Modifier.weight(1f)) {
                EtaxiTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = "Model",
                    placeholder = "Corolla Quest",
                )
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

        Spacer(Modifier.height(24.dp))

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

        Spacer(Modifier.height(32.dp))

        EtaxiPrimaryButton(
            text = "Continue to documents",
            enabled = canSave,
            loading = saving,
            onClick = {
                scope.launch {
                    saving = true
                    repo.addVehicle(make, model, yearInt, color, plate, vehicleClass)
                    saving = false
                    onComplete()
                }
            },
        )

        Spacer(Modifier.height(12.dp))
        Text(
            "Your car's details are sent for approval. We'll review within 24-48 hours and let you know.",
            style = MaterialTheme.typography.bodySmall,
            color = EtaxiColors.InkFaint,
        )
        Spacer(Modifier.height(32.dp))
    }
}
