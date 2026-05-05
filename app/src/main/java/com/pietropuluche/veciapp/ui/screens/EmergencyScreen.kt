package com.pietropuluche.veciapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.UiOption
import com.pietropuluche.veciapp.data.model.emergencyTypeOptions
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.OptionalImagePicker
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.common.uriToCompressedDataUrl
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun EmergencyScreen(
    successMessage: String,
    errorMessage: String,
    onSubmit: (String, Double?, Double?, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedType by rememberSaveable { mutableStateOf(emergencyTypeOptions.first().id) }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var evidenceImageBase64 by remember { mutableStateOf<String?>(null) }
    var imageErrorMessage by remember { mutableStateOf("") }
    var isProcessingImage by remember { mutableStateOf(false) }

    fun fetchLocation() {
        requestCurrentLocation(
            context = context,
            onLocation = { lat, lon ->
                latitude = lat.toString()
                longitude = lon.toString()
            },
            onError = {}
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            fetchLocation()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedImageUri = uri.toString()
        imageErrorMessage = ""
        isProcessingImage = true
        scope.launch {
            val encoded = uriToCompressedDataUrl(context, uri)
            if (encoded == null) {
                selectedImageUri = null
                evidenceImageBase64 = null
                imageErrorMessage = "No se pudo procesar la imagen seleccionada."
            } else {
                evidenceImageBase64 = encoded
            }
            isProcessingImage = false
        }
    }

    ScreenContainer(
        title = "Alerta de emergencia",
        subtitle = "Envia una alerta a la autoridad mas cercana con tu ubicacion actual."
    ) {
        SectionCard {
            InlineMessage(errorMessage, true)
            InlineMessage(successMessage, false)
            InlineMessage(imageErrorMessage, true)
            OptionSelector(
                label = "Tipo de emergencia",
                options = emergencyTypeOptions,
                selectedId = selectedType,
                onSelect = { selectedType = it }
            )
            OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), label = { Text("Referencia") })
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notas") })
            OutlinedTextField(latitude, { latitude = it }, Modifier.fillMaxWidth(), label = { Text("Latitud") })
            OutlinedTextField(longitude, { longitude = it }, Modifier.fillMaxWidth(), label = { Text("Longitud") })
            Text("Puedes escribir coordenadas manualmente o usar tu ubicacion actual.", color = TextSecondary)
            OptionalImagePicker(
                selectedImageUri = selectedImageUri,
                isProcessing = isProcessingImage,
                onPickImage = { imagePickerLauncher.launch("image/*") },
                onClearImage = {
                    selectedImageUri = null
                    evidenceImageBase64 = null
                    imageErrorMessage = ""
                }
            )
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
                Text("Usar ubicacion actual")
            }
            Button(
                onClick = {
                    onSubmit(
                        selectedType,
                        latitude.toDoubleOrNull(),
                        longitude.toDoubleOrNull(),
                        address,
                        notes,
                        evidenceImageBase64
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessingImage
            ) {
                Text("Enviar alerta")
            }
        }
    }
}

@Composable
fun OptionSelector(
    label: String,
    options: List<UiOption>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Text(label)
    options.forEach { option ->
        Button(
            onClick = { onSelect(option.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            val prefix = if (option.id == selectedId) "Seleccionado: " else ""
            Text(prefix + option.label)
        }
    }
}
