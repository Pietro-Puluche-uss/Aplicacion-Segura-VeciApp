package com.pietropuluche.veciapp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

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
