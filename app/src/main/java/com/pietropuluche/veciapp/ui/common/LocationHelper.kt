package com.pietropuluche.veciapp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("MissingPermission")
fun requestCurrentLocation(
    context: Context,
    onLocation: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocation(location.latitude, location.longitude)
            } else {
                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { current ->
                    if (current != null) {
                        onLocation(current.latitude, current.longitude)
                    } else {
                        onError("No pudimos obtener la ubicacion actual")
                    }
                }.addOnFailureListener {
                    onError(it.message ?: "No pudimos obtener la ubicacion actual")
                }
            }
        }
        .addOnFailureListener {
            onError(it.message ?: "No pudimos obtener la ubicacion actual")
        }
}

suspend fun resolveAddressReference(
    context: Context,
    latitude: Double,
    longitude: Double
): String? = withContext(Dispatchers.IO) {
    runCatching {
        if (!Geocoder.isPresent()) return@runCatching null
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val address = addresses?.firstOrNull() ?: return@runCatching null
        listOfNotNull(
            address.thoroughfare,
            address.subThoroughfare,
            address.subLocality,
            address.locality
        ).joinToString(", ").ifBlank { null }
    }.getOrNull()
}
