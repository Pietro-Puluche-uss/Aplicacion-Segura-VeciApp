package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pietropuluche.veciapp.data.model.HistoryDetailUi
import com.pietropuluche.veciapp.data.model.HistoryItemResponse
import com.pietropuluche.veciapp.ui.common.Base64DataUrlImage
import com.pietropuluche.veciapp.ui.common.EmptyState
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.AlertRed
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SuccessGreen
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val SpanishColombiaLocale: Locale = Locale.forLanguageTag("es-CO")

@Composable
fun HistoryScreen(
    history: List<HistoryItemResponse>,
    selectedDetail: HistoryDetailUi?,
    isDetailLoading: Boolean,
    onSelectItem: (HistoryItemResponse) -> Unit,
    onClose: () -> Unit,
    onCloseDetail: () -> Unit
) {
    when {
        isDetailLoading -> {
            HistoryLoadingScreen(onClose = onCloseDetail)
        }

        selectedDetail != null -> {
            HistoryDetailScreen(
                detail = selectedDetail,
                onClose = onCloseDetail
            )
        }

        else -> {
            HistoryListScreen(
                history = history,
                onSelectItem = onSelectItem,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun HistoryListScreen(
    history: List<HistoryItemResponse>,
    onSelectItem: (HistoryItemResponse) -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            HistoryHeader(title = "Historial", onClose = onClose)
        }
        item {
            if (history.isEmpty()) {
                EmptyState("Todavia no hay registros en tu historial.")
            }
        }
        items(history.size) { index ->
            val item = history[index]
            HistoryListCard(
                item = item,
                onClick = { onSelectItem(item) }
            )
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }
    }
}

@Composable
private fun HistoryLoadingScreen(onClose: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            HistoryHeader(title = "Detalle del historial", onClose = onClose)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Text(
                        text = "Cargando detalle...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailScreen(
    detail: HistoryDetailUi,
    onClose: () -> Unit
) {
    var expandedImageData by rememberSaveable { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                HistoryHeader(title = "Detalle", onClose = onClose)
            }
            item {
                HistoryDetailCard(
                    detail = detail,
                    onExpandImage = { expandedImageData = it }
                )
            }
            item { Spacer(modifier = Modifier.height(14.dp)) }
        }

        expandedImageData?.let { imageData ->
            ExpandedHistoryImageDialog(
                imageData = imageData,
                onClose = { expandedImageData = null }
            )
        }
    }
}

@Composable
private fun HistoryHeader(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
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

@Composable
private fun HistoryListCard(
    item: HistoryItemResponse,
    onClick: () -> Unit
) {
    val accentColor = if (item.itemType.equals("emergency", ignoreCase = true)) AlertRed else DeepOcean
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0xFFE8EDF5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.itemType.equals("emergency", ignoreCase = true)) {
                                Icons.Outlined.WarningAmber
                            } else {
                                Icons.Outlined.ReportProblem
                            },
                            contentDescription = null,
                            tint = accentColor
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.title.ifBlank { "Sin titulo" },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        HistoryMetaRow(
                            icon = Icons.Outlined.AccessTime,
                            text = formatFriendlyDateTime(item.createdAt)
                        )
                        HistoryMetaRow(
                            icon = Icons.Outlined.LocationOn,
                            text = item.location?.ifBlank { "Sin ubicacion registrada" }
                                ?: "Sin ubicacion registrada"
                        )
                        HistoryLabelChip(
                            label = item.subtitle.ifBlank { defaultChipLabel(item.itemType) }
                        )
                    }
                }
                StatusChip(status = item.status)
            }
        }
    }
}

@Composable
private fun HistoryDetailCard(
    detail: HistoryDetailUi,
    onExpandImage: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0xFFE8EDF5))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = detail.title.ifBlank { "Detalle" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    detail.categoryOrTypeLabel
                        ?.takeIf { it.isNotBlank() }
                        ?.let { HistoryLabelChip(label = it) }
                }
                StatusChip(status = detail.status)
            }

            HistoryMetaRow(
                icon = Icons.Outlined.AccessTime,
                text = formatFriendlyDateTime(detail.createdAt)
            )

            detail.location
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    HistoryMetaRow(
                        icon = Icons.Outlined.LocationOn,
                        text = it
                    )
                }

            if (detail.itemType.equals("emergency", ignoreCase = true)) {
                detail.assignedAuthorityName
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        DetailInfoBlock(
                            title = "Comisaria asignada",
                            value = it
                        )
                    }
                val etaText = buildEtaText(detail.assignedDistanceKm, detail.estimatedResponseMinutes)
                if (etaText.isNotBlank()) {
                    DetailInfoBlock(
                        title = "Respuesta estimada",
                        value = etaText
                    )
                }
            }

            detail.description
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    DetailInfoBlock(
                        title = if (detail.itemType.equals("emergency", ignoreCase = true)) "Notas" else "Descripcion",
                        value = it
                    )
                }

            detail.evidenceImageBase64
                ?.takeIf { it.isNotBlank() }
                ?.let { imageData ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Imagen adjunta",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Toca la imagen para verla completa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Base64DataUrlImage(
                            dataUrl = imageData,
                            contentDescription = "Imagen del historial",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable { onExpandImage(imageData) }
                        )
                    }
                }

            if (detail.location.isNullOrBlank() &&
                detail.description.isNullOrBlank() &&
                detail.evidenceImageBase64.isNullOrBlank() &&
                detail.assignedAuthorityName.isNullOrBlank()
            ) {
                Text(
                    text = "Este registro no tiene mas detalles complementarios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ExpandedHistoryImageDialog(
    imageData: String,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Base64DataUrlImage(
                dataUrl = imageData,
                contentDescription = "Imagen ampliada del historial",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar imagen",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun DetailInfoBlock(
    title: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun HistoryMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, background, textColor) = statusChipStyle(status)
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = background),
            border = BorderStroke(1.dp, background.copy(alpha = 0.9f))
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun HistoryLabelChip(label: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5FA))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DeepOcean,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun defaultChipLabel(itemType: String): String {
    return if (itemType.equals("emergency", ignoreCase = true)) {
        "Emergencia"
    } else {
        "Reporte"
    }
}

private fun statusChipStyle(status: String): Triple<String, Color, Color> {
    return when (status.uppercase(Locale.getDefault())) {
        "COMPLETED" -> Triple("Completado", Color(0xFFEAFBF0), SuccessGreen)
        "IN_PROGRESS" -> Triple("En progreso", Color(0xFFEAF1FF), Color(0xFF3A67E8))
        "SUBMITTED", "PENDING" -> Triple("Pendiente", Color(0xFFFFF7DF), AlertAmber)
        else -> Triple(status.replaceFirstChar { it.titlecase(Locale.getDefault()) }, Color(0xFFF1F4F8), DeepOcean)
    }
}

private fun buildEtaText(distanceKm: Double?, etaMinutes: Int?): String {
    val parts = buildList {
        if (distanceKm != null) add("A ${String.format(Locale.US, "%.1f", distanceKm)} km")
        if (etaMinutes != null) add("Tiempo estimado: $etaMinutes min")
    }
    return parts.joinToString(" - ")
}

internal fun formatFriendlyDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "Sin fecha registrada"
    return runCatching {
        val dateTime = OffsetDateTime.parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
        val now = java.time.LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm", SpanishColombiaLocale)
        val timeText = dateTime.format(formatter)
        when {
            dateTime.toLocalDate() == now.toLocalDate() -> "Hoy a las $timeText"
            dateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> "Ayer a las $timeText"
            else -> {
                val dayFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'a las' HH:mm", SpanishColombiaLocale)
                dateTime.format(dayFormatter)
            }
        }
    }.getOrElse {
        value.replace("T", " ").replace("Z", "").take(16)
    }
}
