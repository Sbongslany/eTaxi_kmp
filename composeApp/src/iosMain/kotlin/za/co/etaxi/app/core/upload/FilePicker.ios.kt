package za.co.etaxi.app.core.upload

import androidx.compose.runtime.Composable

// iOS stub. Real implementation will use UIDocumentPickerViewController
// or PHPickerViewController when iOS development resumes (project plan
// has iOS deferred until Android is feature complete).
@Composable
actual fun rememberFilePicker(): FilePicker = object : FilePicker {
    override fun pick(onResult: (PickedFile?) -> Unit) {
        println("[FilePicker] iOS picker not implemented yet")
        onResult(null)
    }
}
