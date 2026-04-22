package za.co.etaxi.app.feature.driver.onboarding

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
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.repository.DriverRepository

@Composable
internal fun DocumentsStep(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
) {
    val repo: DriverRepository = koinInject()
    val scope = rememberCoroutineScope()

    var docs by remember { mutableStateOf<List<DriverDocument>>(emptyList()) }
    var uploading by remember { mutableStateOf<DocumentType?>(null) }
    var submitting by remember { mutableStateOf(false) }

    suspend fun refresh() { docs = repo.documents() }
    LaunchedEffect(Unit) { refresh() }

    // Count required docs that have something uploaded
    val requiredDocs = DocumentType.entries.filter { it.required }
    val uploadedCount = docs.count { d ->
        d.type.required && d.status != DocumentStatus.MISSING
    }
    val allRequiredUploaded = uploadedCount == requiredDocs.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = EtaxiColors.Ink)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Your documents",
                        style = MaterialTheme.typography.displaySmall,
                        color = EtaxiColors.Ink,
                    )
                }
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(EtaxiColors.PowerGlow)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        "$uploadedCount / ${requiredDocs.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EtaxiColors.Power,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Upload each document. Submit them all at the end for review.",
                style = MaterialTheme.typography.bodyMedium,
                color = EtaxiColors.InkMuted,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Required docs first
            items(docs.filter { it.type.required }) { doc ->
                DocRow(
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
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "OPTIONAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = EtaxiColors.InkFaint,
                )
                Spacer(Modifier.height(4.dp))
            }
            items(docs.filter { !it.type.required }) { doc ->
                DocRow(
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
            }
        }

        // Sticky submit
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            EtaxiPrimaryButton(
                text = if (allRequiredUploaded) "Submit all for review" else "Upload remaining required docs",
                enabled = allRequiredUploaded && !submitting,
                loading = submitting,
                onClick = {
                    scope.launch {
                        submitting = true
                        repo.submitOnboarding()
                        submitting = false
                        onSubmitted()
                    }
                },
            )
        }
    }
}

@Composable
private fun DocRow(
    doc: DriverDocument,
    loading: Boolean,
    onUpload: () -> Unit,
) {
    val actionable = doc.status == DocumentStatus.MISSING || doc.status == DocumentStatus.REJECTED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                when (doc.status) {
                    DocumentStatus.PENDING  -> EtaxiColors.Warning.copy(alpha = 0.4f)
                    DocumentStatus.APPROVED -> EtaxiColors.Success.copy(alpha = 0.4f)
                    DocumentStatus.REJECTED -> EtaxiColors.Danger.copy(alpha = 0.4f)
                    else                    -> EtaxiColors.SlidesEdge
                },
                RoundedCornerShape(14.dp),
            )
            .then(if (actionable) Modifier.clickable(enabled = !loading, onClick = onUpload) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (icon, tint) = when (doc.status) {
            DocumentStatus.APPROVED -> Icons.Rounded.CheckCircle to EtaxiColors.Success
            DocumentStatus.PENDING  -> Icons.Rounded.HourglassTop to EtaxiColors.Warning
            DocumentStatus.REJECTED -> Icons.Rounded.Upload to EtaxiColors.Danger
            else                    -> Icons.Rounded.Upload to EtaxiColors.Power
        }
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(doc.type.label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(
                when (doc.status) {
                    DocumentStatus.MISSING  -> "Tap to upload"
                    DocumentStatus.PENDING  -> "Uploaded · awaiting review"
                    DocumentStatus.APPROVED -> "Approved"
                    DocumentStatus.REJECTED -> "Rejected · tap to re-upload"
                    DocumentStatus.EXPIRED  -> "Expired · tap to update"
                },
                style = MaterialTheme.typography.bodySmall,
                color = tint,
            )
        }
        if (loading) {
            Text("…", style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.Power)
        }
    }
}
