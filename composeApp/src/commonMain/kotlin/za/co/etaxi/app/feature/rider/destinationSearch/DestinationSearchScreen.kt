package za.co.etaxi.app.feature.rider.destinationSearch

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.data.mock.MockData
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.repository.RiderRepository

// Destination search screen.
//
// Used as both the pickup picker and the dropoff picker (the caller
// decides what to do with the chosen Place). Returned via the
// DestinationPickerBus singleton because Voyager doesn't have a clean
// "pop with a result" API in this codebase. The caller registers a
// callback before pushing this screen, the user picks, and the
// callback fires before the screen pops.
//
// Search is local-only for now (filters savedPlaces + popularPlaces by
// substring). When a Places API endpoint lands on the backend we swap
// the filtering for a real network call.
class DestinationSearchScreen(
    private val title: String,
    private val initialQuery: String = "",
) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: RiderRepository = koinInject()

        var query by remember { mutableStateOf(initialQuery) }
        var saved by remember { mutableStateOf<List<Place>>(emptyList()) }

        LaunchedEffect(Unit) {
            saved = runCatching { repo.savedPlaces() }.getOrDefault(emptyList())
        }

        val popular = remember { MockData.popularPlaces }

        // Combined searchable list: saved + popular, deduped by address.
        val all by remember(saved) {
            derivedStateOf {
                (saved + popular).distinctBy { it.address }
            }
        }
        val results by remember {
            derivedStateOf {
                if (query.isBlank()) all
                else all.filter {
                    it.label.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 48.dp),
        ) {
            // Top app bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.SlidesRaised)
                        .clickable { nav.pop() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = EtaxiColors.Ink)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = EtaxiColors.Ink,
                )
            }
            Spacer(Modifier.height(16.dp))

            // Search field
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EtaxiColors.SlidesRaised)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, null, tint = EtaxiColors.InkMuted)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    cursorBrush = SolidColor(EtaxiColors.Power),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    textStyle = TextStyle(
                        color = EtaxiColors.Ink,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                "Search address or place",
                                color = EtaxiColors.InkFaint,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        inner()
                    },
                )
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { query = "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Close, null,
                            tint = EtaxiColors.InkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Saved places header
            if (saved.isNotEmpty() && query.isBlank()) {
                Text(
                    "Saved places",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.InkFaint,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            // Results
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(results) { place ->
                    PlaceRow(
                        place = place,
                        onClick = {
                            DestinationPickerBus.deliver(place)
                            nav.pop()
                        },
                    )
                }
                if (results.isEmpty()) {
                    item {
                        Text(
                            "No matches",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EtaxiColors.InkFaint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(place: Place, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(EtaxiColors.SlidesRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.LocationOn, null, tint = EtaxiColors.Power)
        }
        Spacer(Modifier.width(14.dp))
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

// Simple in-memory bus for returning the chosen Place from the picker
// back to the caller. Voyager's navigation API in this codebase doesn't
// natively support passing results back on pop, so we use a singleton
// that the caller registers a callback on, and the picker delivers
// into. Cleared on each new registration so stale callbacks don't fire.
object DestinationPickerBus {
    private var pending: ((Place) -> Unit)? = null

    fun await(onPicked: (Place) -> Unit) {
        pending = onPicked
    }

    fun deliver(place: Place) {
        val cb = pending
        pending = null
        cb?.invoke(place)
    }

    fun cancel() {
        pending = null
    }
}
