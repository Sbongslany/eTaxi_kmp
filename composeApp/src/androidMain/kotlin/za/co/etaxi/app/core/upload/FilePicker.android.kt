package za.co.etaxi.app.core.upload

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Android implementation. Uses ActivityResultContracts.GetContent("image/*")
// to launch the system picker. When the user selects a file, we read the
// bytes through ContentResolver and forward to the pending callback.
//
// One callback at a time. If pick() is called twice rapidly, the second
// callback wins (older one is dropped).
@Composable
actual fun rememberFilePicker(): FilePicker {
    val context = LocalContext.current
    val pendingCallback = remember { mutableStateOf<((PickedFile?) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val cb = pendingCallback.value
        pendingCallback.value = null
        if (uri == null) {
            cb?.invoke(null)
            return@rememberLauncherForActivityResult
        }
        try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                cb?.invoke(null)
            } else {
                cb?.invoke(PickedFile(bytes = bytes, mimeType = mime))
            }
        } catch (t: Throwable) {
            println("[FilePicker] read failed: ${t.message}")
            cb?.invoke(null)
        }
    }

    return remember(launcher) {
        object : FilePicker {
            override fun pick(onResult: (PickedFile?) -> Unit) {
                pendingCallback.value = onResult
                launcher.launch("image/*")
            }
        }
    }
}
