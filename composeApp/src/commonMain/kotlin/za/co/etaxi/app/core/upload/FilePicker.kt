package za.co.etaxi.app.core.upload

import androidx.compose.runtime.Composable

// Cross-platform file picker abstraction.
//
// Implementations:
//   - androidMain: launches ACTION_OPEN_DOCUMENT for image/* MIME types,
//     reads bytes via ContentResolver.
//   - iosMain: stub (returns null until iOS work resumes per project plan)
//
// Usage from any composable:
//   val picker = rememberFilePicker()
//   ...
//   onClick = { picker.pick { bytes, mime -> ... } }
data class PickedFile(val bytes: ByteArray, val mimeType: String)

interface FilePicker {
    fun pick(onResult: (PickedFile?) -> Unit)
}

@Composable
expect fun rememberFilePicker(): FilePicker
