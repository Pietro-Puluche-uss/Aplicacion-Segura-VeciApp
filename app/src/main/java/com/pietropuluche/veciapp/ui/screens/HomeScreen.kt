package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pietropuluche.veciapp.ui.common.EmptyState
import com.pietropuluche.veciapp.ui.common.LoadingBlock
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.common.StatCard
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.AlertRed
import com.pietropuluche.veciapp.ui.theme.SuccessGreen
import com.pietropuluche.veciapp.ui.viewmodel.VeciAppUiState

@Composable
fun HomeScreen(
    uiState: VeciAppUiState,
    onRefresh: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    ScreenContainer(
        title = "Centro VeciApp",
        subtitle = uiState.profile?.fullName?.let { "Hola, $it" } ?: "Monitorea tu seguridad vecinal."
    ) {
        if (uiState.isLoading && uiState.profile == null) {
            LoadingBlock("Sincronizando panel...")
        }

        SectionCard {
            Text(
                text = "Acciones rapidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onOpenEmergency,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Text(" Emergencia")
            }
            Button(
                onClick = onOpenReport,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Text(" Reportar incidente")
            }
            Button(
                onClick = onOpenSubscription,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                Text(" Ver planes")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Autoridades cercanas",
                value = "${uiState.dashboard?.nearbyAuthorities ?: 0}",
                accent = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Reportes hoy",
                value = "${uiState.dashboard?.reportsToday ?: 0}",
                accent = AlertAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Mi plan",
                value = uiState.subscription?.currentPlan ?: uiState.dashboard?.subscriptionPlan ?: "-",
                accent = AlertRed,
                modifier = Modifier.weight(1f)
            )
        }

        SectionCard {
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (uiState.history.isEmpty()) {
                EmptyState("Aun no hay actividad registrada en tu cuenta.")
            } else {
                uiState.history.take(3).forEach { item ->
                    Text(item.title, fontWeight = FontWeight.SemiBold)
                    Text(item.subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(item.status, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        SectionCard {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar panel")
            }
        }
    }
}
