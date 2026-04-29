package za.co.etaxi.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import za.co.etaxi.app.domain.model.LatLng
import kotlin.coroutines.resume

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
        3_000L,
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

    @SuppressLint("MissingPermission")
    override suspend fun lastKnownLocation(): LatLng? {
        if (!hasPermission()) return null
        // Try the cached lastLocation first — it's instant if available.
        val cached: LatLng? = try {
            suspendCancellableCoroutine { cont ->
                client.lastLocation
                    .addOnSuccessListener { loc ->
                        cont.resume(loc?.let { LatLng(it.latitude, it.longitude) })
                    }
                    .addOnFailureListener { cont.resume(null) }
            }
        } catch (_: Throwable) {
            null
        }
        if (cached != null) return cached

        // Cache miss — request a fresh fix with a 5-second timeout.
        return withTimeoutOrNull(5_000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    client.getCurrentLocation(
                        CurrentLocationRequest.Builder()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .build(),
                        null,
                    )
                        .addOnSuccessListener { loc ->
                            cont.resume(loc?.let { LatLng(it.latitude, it.longitude) })
                        }
                        .addOnFailureListener { cont.resume(null) }
                } catch (_: Throwable) {
                    cont.resume(null)
                }
            }
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
