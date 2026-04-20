package za.co.etaxi.app.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import za.co.etaxi.app.core.theme.EtaxiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtaxiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    supportingText: String? = null,
    error: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leading?.let { { Icon(it, contentDescription = null) } },
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        supportingText = supportingText?.let { { Text(it) } },
        isError = error,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EtaxiColors.Power,
            unfocusedBorderColor = EtaxiColors.SlidesEdge,
            focusedLabelColor = EtaxiColors.Power,
            unfocusedLabelColor = EtaxiColors.InkMuted,
            cursorColor = EtaxiColors.Power,
            focusedTextColor = EtaxiColors.Ink,
            unfocusedTextColor = EtaxiColors.Ink,
            focusedLeadingIconColor = EtaxiColors.Power,
            unfocusedLeadingIconColor = EtaxiColors.InkMuted,
        ),
    )
}
