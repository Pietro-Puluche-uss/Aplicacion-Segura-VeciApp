package com.pietropuluche.veciapp.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.EmergencyResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation
import com.pietropuluche.veciapp.ui.common.resolveAddressReference
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.AlertRed
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SuccessGreen
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

private const val DefaultEmergencyType = "THREAT"
private const val EmergencyPhoneNumber = "105"

@Composable
fun EmergencyScreen(
    errorMessage: String,
    emergencyConfirmation: EmergencyResponse?,
    onSubmit: (String, Double?, Double?, String, String, String?) -> Unit,
    onGoHome: () -> Unit,
    onClose: () -> Unit
) {
    if (emergencyConfirmation != null) {
        EmergencySentScreen(
            emergency = emergencyConfirmation,
            onGoHome = onGoHome
        )
    } else {
        EmergencyRequestScreen(
            errorMessage = errorMessage,
            onSubmit = onSubmit,
            onClose = onClose
        )
    }
}

@Composable
private fun EmergencyRequestScreen(
    errorMessage: String,
    onSubmit: (String, Double?, Double?, String, String, String?) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var locationDisplay by rememberSaveable { mutableStateOf("") }
    var addressReference by rememberSaveable { mutableStateOf("") }
    var locationError by rememberSaveable { mutableStateOf("") }
    var isLocating by rememberSaveable { mutableStateOf(true) }

    fun fetchLocation() {
        isLocating = true
        locationError = ""
        requestCurrentLocation(
            context = context,
            onLocation = { lat, lon ->
                latitude = lat
                longitude = lon
                locationDisplay = formatCoordinates(lat, lon)
                addressReference = locationDisplay
                isLocating = false
                scope.launch {
                    resolveAddressReference(context, lat, lon)?.let { resolved ->
                        addressReference = resolved
                    }
                }
            },
            onError = { message ->
                latitude = null
                longitude = null
                locationDisplay = ""
                addressReference = ""
                locationError = message
                isLocating = false
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            fetchLocation()
        } else {
            isLocating = false
            locationError = "Necesitamos tu ubicacion para enviar la alerta."
        }
    }

    LaunchedEffect(Unit) {
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
    }

    val locationText = addressReference.ifBlank { locationDisplay }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emergencia",
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
        }
        item { EmergencyHeroCard() }
        item {
            LocationStatusCard(
                isLocating = isLocating,
                locationDisplay = locationText,
                locationError = locationError,
                onRetry = {
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
                }
            )
        }
        item { EmergencyInfoCard() }
        item {
            Button(
                onClick = {
                    onSubmit(
                        DefaultEmergencyType,
                        latitude,
                        longitude,
                        addressReference,
                        "",
                        null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = !isLocating && latitude != null && longitude != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRed,
                    contentColor = SurfaceCard,
                    disabledContainerColor = AlertRed.copy(alpha = 0.45f),
                    disabledContentColor = SurfaceCard
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " CONFIRMAR EMERGENCIA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EmergencyCallShortcut()
        }
    }
}

@Composable
private fun EmergencySentScreen(
    emergency: EmergencyResponse,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(SuccessGreen.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Alerta enviada",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "La comisaria mas cercana ha recibido tu alerta de VIDA EN RIESGO.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            EmergencyAuthorityCard(emergency = emergency)
        }
        item {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:$EmergencyPhoneNumber")
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepOcean,
                    contentColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = null
                )
                Text(
                    text = " Llamar a Comisaria: $EmergencyPhoneNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        item {
            TextButton(onClick = onGoHome) {
                Text(
                    text = "Volver al inicio",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }
        item {
            Text(
                text = "Mantente en un lugar seguro. La ayuda esta en camino.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun EmergencyHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFF3F3)),
        border = BorderStroke(2.dp, AlertRed)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(AlertRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = SurfaceCard,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "VIDA EN RIESGO",
                style = MaterialTheme.typography.headlineSmall,
                color = AlertRed,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Esta alerta notificara inmediatamente a la comisaria mas cercana con tu ubicacion exacta.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LocationStatusCard(
    isLocating: Boolean,
    locationDisplay: String,
    locationError: String,
    onRetry: () -> Unit
) {
    val shouldAllowRetry = !isLocating && locationDisplay.isBlank()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (shouldAllowRetry) {
                    Modifier.clickable(onClick = onRetry)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF2F6FB))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(androidx.compose.ui.graphics.Color(0xFFE5EEFB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tu ubicacion",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                when {
                    isLocating -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Obteniendo ubicacion actual...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    locationDisplay.isNotBlank() -> {
                        Text(
                            text = locationDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    else -> {
                        Text(
                            text = locationError.ifBlank { "No pudimos detectar tu ubicacion." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlertRed
                        )
                        Text(
                            text = "Toca aqui para reintentar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFFAEC)),
        border = BorderStroke(1.dp, AlertAmber.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AlertAmber,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Antes de confirmar",
                    style = MaterialTheme.typography.titleSmall,
                    color = androidx.compose.ui.graphics.Color(0xFF9A6200),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Esta alerta es solo para emergencias reales.\nTu ubicacion sera enviada a las autoridades.\nRecibiras una llamada de confirmacion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmergencyCallShortcut() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "O llama directamente",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:$EmergencyPhoneNumber")
                        )
                    )
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = EmergencyPhoneNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmergencyAuthorityCard(emergency: EmergencyResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEFFBF4)),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = null,
                    tint = SuccessGreen
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = emergency.assignedAuthorityName?.ifBlank { "Comisaria mas cercana" }
                            ?: "Comisaria mas cercana",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = emergency.buildEtaSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SuccessGreen.copy(alpha = 0.22f))
            )
            Text(
                text = "Ubicacion enviada: ${emergency.buildLocationSummary()}",
                style = MaterialTheme.typography.bodyMedium,
                color = SuccessGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun EmergencyResponse.buildEtaSummary(): String {
    val parts = buildList {
        assignedDistanceKm?.let { add("A ${formatOneDecimal(it)} km") }
        estimatedResponseMinutes?.let { add("Tiempo estimado: $it min") }
    }
    return if (parts.isEmpty()) {
        "La autoridad ya fue notificada."
    } else {
        parts.joinToString(" - ")
    }
}

private fun EmergencyResponse.buildLocationSummary(): String {
    return when {
        latitude != null && longitude != null -> formatCoordinates(latitude, longitude)
        !addressReference.isNullOrBlank() -> addressReference
        else -> "Compartida con autoridades"
    }
}

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    return String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
}

private fun formatOneDecimal(value: Double): String {
    return String.format(Locale.US, "%.1f", value)
}
