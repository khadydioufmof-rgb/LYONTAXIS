package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Data model representing real-time GPS positioning for a driver.
 */
data class DriverGpsLocation(
    val latitude: Double = 48.856614,
    val longitude: Double = 2.352222,
    val altitude: Double = 35.0,
    val speedKmh: Float = 0f,
    val bearing: Float = 0f,
    val accuracyMeters: Float = 5f,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "gps",
    val hasGpsFix: Boolean = true
)

/**
 * Interface defining location tracking operations.
 */
interface LocationTracker {
    fun getLocationUpdates(intervalMs: Long = 2000L): Flow<DriverGpsLocation>
    suspend fun getCurrentLocation(): DriverGpsLocation?
    fun hasLocationPermission(): Boolean
}

/**
 * Real-time Android GPS Location Tracker leveraging Google Play Services Location (FusedLocationProviderClient).
 */
class DefaultLocationTracker(
    private val context: Context
) : LocationTracker {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    override fun getLocationUpdates(intervalMs: Long): Flow<DriverGpsLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        ).apply {
            setMinUpdateIntervalMillis(intervalMs / 2)
            setMinUpdateDistanceMeters(1.0f)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toDriverGpsLocation())
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                // If GPS signal is lost or regained
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun getCurrentLocation(): DriverGpsLocation? {
        if (!hasLocationPermission()) return null

        return try {
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
                ).addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
            }
            location?.toDriverGpsLocation()
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Extension mapper from Android android.location.Location to DriverGpsLocation
 */
fun Location.toDriverGpsLocation(): DriverGpsLocation {
    val speedKmh = if (hasSpeed()) speed * 3.6f else 0f
    val bearingDeg = if (hasBearing()) bearing else 0f
    return DriverGpsLocation(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        speedKmh = speedKmh,
        bearing = bearingDeg,
        accuracyMeters = if (hasAccuracy()) accuracy else 10f,
        timestamp = time,
        provider = provider ?: "fused",
        hasGpsFix = true
    )
}
