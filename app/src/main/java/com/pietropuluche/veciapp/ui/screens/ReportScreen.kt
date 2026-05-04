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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.ReportCategoryResponse
import com.pietropuluche.veciapp.data.model.UiOption
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation
import com.pietropuluche.veciapp.ui.theme.TextSecondary

@Composable
fun ReportScreen(
    categories: List<ReportCategoryResponse>,
    successMessage: String,
    errorMessage: String,
    onSubmit: (String, String, String, String, Double?, Double?) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by rememberSaveable { mutableStateOf(categories.firstOrNull()?.id.orEmpty()) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }

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
        if (result.values.any { it }) fetchLocation()
    }

    ScreenContainer(
        title = "Nuevo reporte",
        subtitle = "Registra incidentes comunitarios con ubicacion y descripcion."
    ) {
        SectionCard {
            InlineMessage(errorMessage, true)
            InlineMessage(successMessage, false)
            if (categories.isNotEmpty()) {
                OptionSelector(
                    label = "Categoria",
                    options = categories.map { UiOption(it.id, it.label) },
                    selectedId = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Titulo") })
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descripcion") })
            OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), label = { Text("Referencia") })
            OutlinedTextField(latitude, { latitude = it }, Modifier.fillMaxWidth(), label = { Text("Latitud") })
            OutlinedTextField(longitude, { longitude = it }, Modifier.fillMaxWidth(), label = { Text("Longitud") })
            Text("Puedes completar las coordenadas automaticamente.", color = TextSecondary)
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
                        selectedCategory,
                        title,
                        description,
                        address,
                        latitude.toDoubleOrNull(),
                        longitude.toDoubleOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCategory.isNotBlank() && title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Enviar reporte")
            }
        }
    }
}
