package com.pietropuluche.veciapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.ProfileResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation

@Composable
fun ProfileScreen(
    profile: ProfileResponse?,
    successMessage: String,
    errorMessage: String,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onUpdateLocation: (Double, Double, String, String) -> Unit,
    onOpenSubscription: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var documentNumber by rememberSaveable { mutableStateOf("") }
    var photoUrl by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(profile?.userId) {
        if (profile != null) {
            firstName = profile.firstName
            lastName = profile.lastName
            phone = profile.phone
            documentNumber = profile.documentNumber.orEmpty()
            photoUrl = profile.profilePhotoUrl.orEmpty()
            district = profile.district.orEmpty()
            city = profile.city.orEmpty()
            latitude = profile.currentLatitude?.toString().orEmpty()
            longitude = profile.currentLongitude?.toString().orEmpty()
        }
    }

    fun fetchLocation() {
        requestCurrentLocation(
            context = context,
            onLocation = { lat, lon ->
                latitude = lat.toString()
                longitude = lon.toString()
                onUpdateLocation(lat, lon, district, city)
            },
            onError = {}
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) fetchLocation()
    }

    ScreenContainer(
        title = "Perfil",
        subtitle = profile?.email ?: "Gestiona tu informacion personal."
    ) {
        SectionCard {
            InlineMessage(errorMessage, true)
            InlineMessage(successMessage, false)
            OutlinedTextField(firstName, { firstName = it }, Modifier.fillMaxWidth(), label = { Text("Nombres") })
            OutlinedTextField(lastName, { lastName = it }, Modifier.fillMaxWidth(), label = { Text("Apellidos") })
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Telefono") })
            OutlinedTextField(documentNumber, { documentNumber = it }, Modifier.fillMaxWidth(), label = { Text("Documento") })
            OutlinedTextField(photoUrl, { photoUrl = it }, Modifier.fillMaxWidth(), label = { Text("URL de foto") })
            Button(
                onClick = { onSaveProfile(firstName, lastName, phone, documentNumber, photoUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar perfil")
            }
        }

        SectionCard {
            OutlinedTextField(district, { district = it }, Modifier.fillMaxWidth(), label = { Text("Distrito") })
            OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("Ciudad") })
            OutlinedTextField(latitude, { latitude = it }, Modifier.fillMaxWidth(), label = { Text("Latitud") })
            OutlinedTextField(longitude, { longitude = it }, Modifier.fillMaxWidth(), label = { Text("Longitud") })
            Button(
                onClick = {
                    onUpdateLocation(
                        latitude.toDoubleOrNull() ?: return@Button,
                        longitude.toDoubleOrNull() ?: return@Button,
                        district,
                        city
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar ubicacion")
            }
            Button(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted || coarseGranted) {
                        fetchLocation()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar mi ubicacion actual")
            }
        }

        SectionCard {
            Button(onClick = onOpenSubscription, modifier = Modifier.fillMaxWidth()) {
                Text("Administrar suscripcion")
            }
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesion")
            }
        }
    }
}
