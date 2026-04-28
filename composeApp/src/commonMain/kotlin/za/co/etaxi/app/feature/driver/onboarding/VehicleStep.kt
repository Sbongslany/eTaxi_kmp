package za.co.etaxi.app.feature.driver.onboarding

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.repository.DriverRepository

// VehicleStep — collects the driver's car details and creates a Vehicle
// record on the backend.
//
// initialVehicle: pre-existing vehicle from a prior session. When non-null
// the form is pre-filled and the Continue button skips the addVehicle call
// (the record already exists). Lets a driver who killed the app mid-
// onboarding pick up where they left off.
@Composable
internal fun VehicleStep(
    initialVehicle: DriverVehicle? = null,
    onComplete: () -> Unit,
) {
    val repo: DriverRepository = koinInject()
    val scope = rememberCoroutineScope()

    var make by remember { mutableStateOf(initialVehicle?.make.orEmpty()) }
    var model by remember { mutableStateOf(initialVehicle?.model.orEmpty()) }
    var year by remember { mutableStateOf(initialVehicle?.year?.toString().orEmpty()) }
    var color by remember { mutableStateOf(initialVehicle?.color.orEmpty()) }
    var plate by remember { mutableStateOf(initialVehicle?.plateNumber.orEmpty()) }
    var vehicleClass by remember {
        mutableStateOf(initialVehicle?.vehicleClass ?: VehicleClass.STANDARD)
    }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            if (initialVehicle != null) "We've kept your details. Tap continue to proceed."
            else "Tell us about the car you'll be driving.",
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

        Text(
            "VEHICLE CLASS",
            style = MaterialTheme.typography.labelSmall,
            color = EtaxiColors.InkFaint,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleClass.entries.forEach { cls ->
                val selected = cls == vehicleClass
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) EtaxiColors.PowerGlow
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (selected) EtaxiColors.Power else EtaxiColors.SlidesEdge,
                            RoundedCornerShape(14.dp),
                        )
                        .clickable(enabled = initialVehicle == null) { vehicleClass = cls }
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

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = EtaxiColors.Danger,
            )
        }

        Spacer(Modifier.height(32.dp))

        EtaxiPrimaryButton(
            text = "Continue to documents",
            enabled = canSave || initialVehicle != null,
            loading = saving,
            onClick = {
                if (initialVehicle != null) {
                    onComplete()
                    return@EtaxiPrimaryButton
                }
                scope.launch {
                    saving = true
                    errorMessage = null
                    try {
                        repo.addVehicle(make, model, yearInt, color, plate, vehicleClass)
                        onComplete()
                    } catch (t: Throwable) {
                        errorMessage = t.message ?: "Could not save vehicle. Try again."
                    } finally {
                        saving = false
                    }
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
