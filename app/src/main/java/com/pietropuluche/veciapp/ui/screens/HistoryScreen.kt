package com.pietropuluche.veciapp.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pietropuluche.veciapp.data.model.HistoryItemResponse
import com.pietropuluche.veciapp.ui.common.EmptyState
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.theme.TextSecondary

@Composable
fun HistoryScreen(history: List<HistoryItemResponse>) {
    ScreenContainer(
        title = "Historial",
        subtitle = "Consulta reportes y emergencias registradas."
    ) {
        if (history.isEmpty()) {
            EmptyState("Todavia no hay registros en tu historial.")
        } else {
            history.forEach { item ->
                SectionCard {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.subtitle, color = TextSecondary)
                    Text(item.status, color = MaterialTheme.colorScheme.primary)
                    Text(
                        item.location?.ifBlank { "Sin referencia de ubicacion" } ?: "Sin referencia de ubicacion",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(formatDateTime(item.createdAt), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

internal fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return value.replace("T", " ").replace("Z", "").take(16)
}
