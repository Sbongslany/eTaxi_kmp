package za.co.etaxi.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import za.co.etaxi.app.domain.model.LatLng

// Android implementation. Uses FusedLocationProviderClient with HIGH_ACCURACY
// at 3-second intervals — same cadence as the simulated location we
// replaced. No foreground service: the driver is expected to keep the
// active-trip screen visible. If the app is backgrounded, location
// updates pause until they return; that's acceptable for v1.
//
// Permission: caller MUST request ACCESS_FINE_LOCATION at runtime
// before start(). If permission is missing start() is a no-op.
class AndroidLocationStreamer(
    private val context: Context,
) : LocationStreamer {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val _locations = MutableSharedFlow<LatLng>(
        replay = 1,
        extraBufferCapacity = 16,
    )
    override val locations: SharedFlow<LatLng> = _locations.asSharedFlow()

    private val request: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        3_000L,  // 3 seconds
    ).setMinUpdateIntervalMillis(1_500L).build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val last = result.lastLocation ?: return
            _locations.tryEmit(LatLng(last.latitude, last.longitude))
        }
    }

    private var running = false

    @SuppressLint("MissingPermission")
    override fun start() {
        if (running) return
        if (!hasPermission()) return
        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            running = true
        } catch (t: Throwable) {
            println("[LocationStreamer] start failed: ${t.message}")
        }
    }

    override fun stop() {
        if (!running) return
        try {
            client.removeLocationUpdates(callback)
        } catch (_: Throwable) {
        }
        running = false
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
