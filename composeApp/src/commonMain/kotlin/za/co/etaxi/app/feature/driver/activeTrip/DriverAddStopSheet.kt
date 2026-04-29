package za.co.etaxi.app.feature.driver.activeTrip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiTextField
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.data.mock.MockData
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.repository.DriverRepository

// Driver-side add-stop sheet. Identical UX to the rider's, but calls
// DriverRepository.addStop instead of RiderRepository.addStop. The
// backend handles either side.
@Composable
fun DriverAddStopSheet(
    visible: Boolean,
    tripId: String?,
    onDismiss: () -> Unit,
    onAdded: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    val repo: DriverRepository = koinInject()
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
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
                    "Add a stop",
                    style = MaterialTheme.typography.titleLarge,
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

            Spacer(Modifier.height(6.dp))
            Text(
                "Backend will recalculate the fare automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkMuted,
            )

            Spacer(Modifier.height(16.dp))
            EtaxiTextField(
                value = query,
                onValueChange = { query = it },
                label = "Search",
                placeholder = "Street, landmark or place",
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.Danger)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "POPULAR IN JOHANNESBURG",
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(8.dp))

            val filtered = remember(query) {
                MockData.popularPlaces.filter {
                    query.isBlank() ||
                        it.label.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
                }
            }

            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(filtered) { place ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !saving) {
                                if (tripId == null) return@clickable
                                scope.launch {
                                    saving = true
                                    error = null
                                    try {
                                        repo.addStop(tripId, place)
                                        onAdded(place)
                                    } catch (t: Throwable) {
                                        error = t.message ?: "Could not add stop"
                                    } finally {
                                        saving = false
                                    }
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EtaxiColors.SlidesRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Place, null, tint = EtaxiColors.Power)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                place.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = EtaxiColors.Ink,
                            )
                            Text(
                                place.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = EtaxiColors.InkFaint,
                            )
                        }
                    }
                }
            }
        }
    }
}
