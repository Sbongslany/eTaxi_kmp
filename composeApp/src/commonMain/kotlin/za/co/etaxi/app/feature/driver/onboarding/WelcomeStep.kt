package za.co.etaxi.app.feature.driver.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.DocumentType

@Composable
internal fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero icon
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(EtaxiColors.Power),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.DirectionsCar,
                null,
                tint = EtaxiColors.InkOnPower,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(24.dp))

        Text(
            "Let's get your car on the road",
            style = MaterialTheme.typography.displaySmall,
            color = EtaxiColors.Ink,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "We need a few things to verify you before accepting trips. Takes about 10 minutes if you have your documents handy.",
            style = MaterialTheme.typography.bodyLarge,
            color = EtaxiColors.InkMuted,
        )

        Spacer(Modifier.height(32.dp))

        // What you'll need card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Text(
                "WHAT YOU'LL NEED",
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(12.dp))

            ChecklistRow("Vehicle details (make, model, year, plate)")
            ChecklistRow("Your SA ID or passport")
            ChecklistRow("Valid driver's licence")
            ChecklistRow("PrDP (professional driving permit)")
            ChecklistRow("Vehicle registration (licence disc)")
            ChecklistRow("Roadworthy certificate")
            ChecklistRow("Operating licence issued by your province")
            ChecklistRow("Vehicle insurance certificate")
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Have them as clear photos or PDFs. Blurry scans will be rejected.",
            style = MaterialTheme.typography.bodySmall,
            color = EtaxiColors.InkFaint,
        )

        Spacer(Modifier.height(32.dp))
        EtaxiPrimaryButton(
            text = "I'm ready, let's start",
            onClick = onStart,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ChecklistRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(EtaxiColors.PowerGlow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.Ink)
    }
}

// Quietly resolve type for reference — keeps this file from needing DocumentType directly
@Suppress("unused")
private val requiredDocsRef = DocumentType.entries.filter { it.required }
