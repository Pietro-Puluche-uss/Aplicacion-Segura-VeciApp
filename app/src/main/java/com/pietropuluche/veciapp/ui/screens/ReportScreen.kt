package com.pietropuluche.veciapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.ReportCategoryResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.OptionalImagePicker
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation
import com.pietropuluche.veciapp.ui.common.resolveAddressReference
import com.pietropuluche.veciapp.ui.common.uriToCompressedDataUrl
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private const val ReportDescriptionMaxLength = 500

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    categories: List<ReportCategoryResponse>,
    successMessage: String,
    errorMessage: String,
    onSubmit: (String, String, String, String, Double?, Double?, String?) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf(categories.firstOrNull()?.id.orEmpty()) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var evidenceImageBase64 by remember { mutableStateOf<String?>(null) }
    var imageErrorMessage by remember { mutableStateOf("") }
    var isProcessingImage by remember { mutableStateOf(false) }
    var locationHelperText by rememberSaveable { mutableStateOf("Presiona el boton para obtener tu ubicacion actual.") }

    val selectedCategoryLabel = categories.firstOrNull { it.id == selectedCategory }?.label.orEmpty()

    fun fetchLocation() {
        locationHelperText = "Obteniendo ubicacion actual..."
        requestCurrentLocation(
            context = context,
            onLocation = { lat, lon ->
                latitude = lat
                longitude = lon
                scope.launch {
                    address = resolveAddressReference(context, lat, lon)
                        ?: "$lat, $lon"
                    locationHelperText = "Ubicacion actual cargada."
                }
            },
            onError = { message ->
                locationHelperText = message
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            fetchLocation()
        } else {
            locationHelperText = "Necesitamos permiso de ubicacion para autocompletar esta referencia."
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hacer un reporte",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary
                    )
                }
            }
        }
        item {
            if (errorMessage.isNotBlank()) {
                InlineMessage(errorMessage, true)
            }
            if (successMessage.isNotBlank()) {
                InlineMessage(successMessage, false)
            }
            if (imageErrorMessage.isNotBlank()) {
                InlineMessage(imageErrorMessage, true)
            }
        }
        item {
            ReportFieldLabel("Categoria")
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategoryLabel,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona una categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DeepOcean,
                        unfocusedBorderColor = DeepOcean,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = {
                                selectedCategory = category.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        item {
            ReportFieldLabel("Titulo del reporte")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Resumen breve del incidente...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }
        item {
            ReportFieldLabel("Descripcion")
            OutlinedTextField(
                value = description,
                onValueChange = {
                    if (it.length <= ReportDescriptionMaxLength) {
                        description = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Describe los detalles del incidente...") },
                shape = RoundedCornerShape(16.dp),
                minLines = 5,
                maxLines = 5
            )
        }
        item {
            Text(
                text = "${description.length}/$ReportDescriptionMaxLength",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            ReportFieldLabel("Ubicacion (opcional)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Direccion o ubicacion") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
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
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepOcean,
                        contentColor = SurfaceCard
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Usar ubicacion actual"
                    )
                }
            }
        }
        item {
            Text(
                text = locationHelperText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE7ECF3))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                }
            }
        }
        item {
            Button(
                onClick = {
                    onSubmit(
                        selectedCategory,
                        title,
                        description,
                        address,
                        latitude,
                        longitude,
                        evidenceImageBase64
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessingImage && selectedCategory.isNotBlank() && title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepOcean,
                    contentColor = SurfaceCard
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "  Enviar reporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }
    }
}

@Composable
private fun ReportFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}
