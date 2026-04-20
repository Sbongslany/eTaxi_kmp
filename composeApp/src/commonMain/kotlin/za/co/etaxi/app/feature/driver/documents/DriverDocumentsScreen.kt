package za.co.etaxi.app.feature.driver.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.repository.DriverRepository

object DriverDocumentsScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val scope = rememberCoroutineScope()

        var docs by remember { mutableStateOf<List<DriverDocument>>(emptyList()) }
        var uploading by remember { mutableStateOf<DocumentType?>(null) }

        suspend fun refresh() { docs = repo.documents() }
        LaunchedEffect(Unit) { refresh() }

        val groupedByStatus = remember(docs) {
            // Order statuses for display: REJECTED > EXPIRED > MISSING > PENDING > APPROVED
            val order = listOf(
                DocumentStatus.REJECTED,
                DocumentStatus.EXPIRED,
                DocumentStatus.MISSING,
                DocumentStatus.PENDING,
                DocumentStatus.APPROVED,
            )
            order.mapNotNull { s ->
                val list = docs.filter { it.status == s }
                if (list.isEmpty()) null else s to list
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 48.dp, bottom = 32.dp),
        ) {
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
                    Text("Documents", style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.Ink)
                    Text(
                        "Keep your documents current to stay online",
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                groupedByStatus.forEach { (status, list) ->
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            statusHeading(status),
                            style = MaterialTheme.typography.labelSmall,
                            color = EtaxiColors.InkFaint,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(list) { doc ->
                        DocumentCard(
                            doc = doc,
                            loading = uploading == doc.type,
                            onUpload = {
                                scope.launch {
                                    uploading = doc.type
                                    repo.uploadDocument(doc.type)
                                    uploading = null
                                    refresh()
                                }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

private fun statusHeading(s: DocumentStatus): String = when (s) {
    DocumentStatus.REJECTED -> "ACTION REQUIRED"
    DocumentStatus.EXPIRED  -> "EXPIRED"
    DocumentStatus.MISSING  -> "MISSING"
    DocumentStatus.PENDING  -> "UNDER REVIEW"
    DocumentStatus.APPROVED -> "APPROVED"
}

@Composable
private fun DocumentCard(
    doc: DriverDocument,
    loading: Boolean,
    onUpload: () -> Unit,
) {
    val (icon, iconTint, borderColor) = when (doc.status) {
        DocumentStatus.APPROVED -> Triple(Icons.Rounded.CheckCircle, EtaxiColors.Success, EtaxiColors.SlidesEdge)
        DocumentStatus.PENDING  -> Triple(Icons.Rounded.HourglassTop, EtaxiColors.Warning, EtaxiColors.Warning.copy(alpha = 0.4f))
        DocumentStatus.REJECTED -> Triple(Icons.Rounded.ErrorOutline, EtaxiColors.Danger, EtaxiColors.Danger.copy(alpha = 0.5f))
        DocumentStatus.EXPIRED  -> Triple(Icons.Rounded.Warning, EtaxiColors.Warning, EtaxiColors.Warning.copy(alpha = 0.5f))
        DocumentStatus.MISSING  -> Triple(Icons.Rounded.Upload, EtaxiColors.Power, EtaxiColors.Power.copy(alpha = 0.4f))
    }
    val actionable = doc.status in listOf(
        DocumentStatus.MISSING,
        DocumentStatus.REJECTED,
        DocumentStatus.EXPIRED,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (actionable) Modifier.clickable(enabled = !loading, onClick = onUpload) else Modifier)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(doc.type.label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
                Spacer(Modifier.height(2.dp))
                Text(
                    statusSubtitle(doc),
                    style = MaterialTheme.typography.bodySmall,
                    color = iconTint,
                )
            }
            if (actionable) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EtaxiColors.PowerGlow)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        if (loading) "UPLOADING…" else "UPLOAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = EtaxiColors.Power,
                    )
                }
            }
        }

        // Rejection reason expanded row
        if (doc.status == DocumentStatus.REJECTED && doc.rejectionReason != null) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(EtaxiColors.Danger.copy(alpha = 0.08f))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Rounded.Info, null, tint = EtaxiColors.Danger, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    doc.rejectionReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = EtaxiColors.Ink,
                )
            }
        }
    }
}

private fun statusSubtitle(doc: DriverDocument): String = when (doc.status) {
    DocumentStatus.APPROVED -> if (doc.expiresAt != null) "Approved · expires ${formatMonth(doc.expiresAt)}" else "Approved"
    DocumentStatus.PENDING  -> "Submitted · awaiting review"
    DocumentStatus.REJECTED -> "Rejected · tap to re-upload"
    DocumentStatus.EXPIRED  -> "Expired · tap to update"
    DocumentStatus.MISSING  -> "Not yet uploaded"
}

private fun formatMonth(instant: kotlinx.datetime.Instant): String {
    // Brief "Dec 2027" style — using kotlinx.datetime bits available cross-platform
    val local = instant.toString().substring(0, 7) // "2026-04"
    val (y, m) = local.split("-").let { it[0] to it[1] }
    val month = when (m) {
        "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
        "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
        "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; else -> "Dec"
    }
    return "$month $y"
}
