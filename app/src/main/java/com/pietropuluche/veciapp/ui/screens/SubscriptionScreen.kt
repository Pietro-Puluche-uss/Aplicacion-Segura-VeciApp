package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pietropuluche.veciapp.data.model.SubscriptionPlanResponse
import com.pietropuluche.veciapp.data.model.UserSubscriptionResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.SuccessGreen
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import java.util.Locale

private data class SubscriptionPlanUi(
    val code: String,
    val title: String,
    val tagline: String,
    val priorityLabel: String,
    val badgeLetter: String,
    val badgeBackground: Color,
    val borderColor: Color,
    val popular: Boolean = false,
    val features: List<String>,
    val usePrimaryAction: Boolean = false
)

private val subscriptionPlanPresets = listOf(
    SubscriptionPlanUi(
        code = "BASIC",
        title = "Basico",
        tagline = "Para empezar a reportar incidencias",
        priorityLabel = "Prioridad baja",
        badgeLetter = "B",
        badgeBackground = Color(0xFFE8EAEE),
        borderColor = DeepOcean,
        features = listOf(
            "Reportes basicos",
            "Cola de atencion estandar",
            "Alertas de emergencia",
            "Historial limitado (7 dias)"
        )
    ),
    SubscriptionPlanUi(
        code = "PREMIUM",
        title = "Premium",
        tagline = "Tus reportes con atencion prioritaria",
        priorityLabel = "Prioridad alta",
        badgeLetter = "P",
        badgeBackground = DeepOcean,
        borderColor = Color(0xFFE2E7F0),
        popular = true,
        features = listOf(
            "Reportes con prioridad alta",
            "Atencion preferente por comisaria",
            "Seguimiento en tiempo real",
            "Historial completo",
            "Notificaciones de estado",
            "Soporte prioritario 24/7"
        ),
        usePrimaryAction = true
    ),
    SubscriptionPlanUi(
        code = "FAMILY",
        title = "Familiar",
        tagline = "Proteccion prioritaria para toda tu familia",
        priorityLabel = "Prioridad maxima",
        badgeLetter = "F",
        badgeBackground = Color(0xFFFFA31A),
        borderColor = Color(0xFFE2E7F0),
        features = listOf(
            "Todo de Premium",
            "Hasta 5 miembros del hogar",
            "Prioridad maxima en cola",
            "Alertas familiares compartidas",
            "Ubicacion en tiempo real",
            "Canal directo con comisaria"
        )
    )
)

@Composable
fun SubscriptionScreen(
    currentSubscription: UserSubscriptionResponse?,
    plans: List<SubscriptionPlanResponse>,
    successMessage: String,
    errorMessage: String,
    onSelectPlan: (String) -> Unit,
    onClose: () -> Unit
) {
    val currentPlanCode = currentSubscription?.currentPlan?.uppercase(Locale.getDefault()).orEmpty()
    val planPriceMap = plans.associateBy { it.code.uppercase(Locale.getDefault()) }
    val renderedPlans = subscriptionPlanPresets.map { preset ->
        val livePlan = planPriceMap[preset.code]
        preset.copy(
            features = if (livePlan?.features.isNullOrEmpty()) preset.features else preset.features
        ) to livePlan
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suscripciones",
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
            Text(
                text = "Los planes premium permiten que tus reportes sean atendidos con mayor prioridad por la comisaria mas cercana.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        item {
            if (errorMessage.isNotBlank()) {
                InlineMessage(errorMessage, true)
            }
            if (successMessage.isNotBlank()) {
                InlineMessage(successMessage, false)
            }
        }
        items(renderedPlans.size) { index ->
            val (planUi, livePlan) = renderedPlans[index]
            SubscriptionPlanCard(
                plan = planUi,
                livePlan = livePlan,
                isCurrentPlan = currentPlanCode == planUi.code,
                onSelectPlan = onSelectPlan
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
            ) {
                Text(
                    text = "Puedes cancelar tu suscripcion en cualquier momento.\nLos pagos se procesan de forma segura.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlanUi,
    livePlan: SubscriptionPlanResponse?,
    isCurrentPlan: Boolean,
    onSelectPlan: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        if (plan.popular) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = DeepOcean)
                ) {
                    Text(
                        text = "Popular",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurfaceCard,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.5.dp, plan.borderColor)
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(plan.badgeBackground, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plan.badgeLetter,
                                color = if (plan.badgeBackground == DeepOcean) SurfaceCard else TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = plan.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    PlanPriceLabel(
                        planCode = plan.code,
                        monthlyPrice = livePlan?.monthlyPrice ?: 0.0,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .widthIn(min = 92.dp)
                    )
                }

                Text(
                    text = plan.priorityLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlertAmber,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    plan.features.forEach { feature ->
                        FeatureRow(text = feature)
                    }
                }

                Button(
                    onClick = { onSelectPlan(plan.code) },
                    enabled = !isCurrentPlan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = if (isCurrentPlan) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF8F8F8),
                            contentColor = Color(0xFF9DA7B3),
                            disabledContainerColor = Color(0xFFF8F8F8),
                            disabledContentColor = Color(0xFF9DA7B3)
                        )
                    } else if (plan.usePrimaryAction) {
                        ButtonDefaults.buttonColors(
                            containerColor = DeepOcean,
                            contentColor = SurfaceCard
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = TextPrimary
                        )
                    },
                    border = if (!isCurrentPlan && !plan.usePrimaryAction) {
                        BorderStroke(1.dp, Color(0xFFD9DEE6))
                    } else {
                        null
                    }
                ) {
                    Text(
                        text = if (isCurrentPlan) {
                            "Plan Actual"
                        } else {
                            "Elegir ${plan.title}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanPriceLabel(
    planCode: String,
    monthlyPrice: Double,
    modifier: Modifier = Modifier
) {
    if (planCode == "BASIC" || monthlyPrice <= 0.0) {
        Text(
            text = "Gratis",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = modifier,
            maxLines = 1
        )
        return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "S/ ${String.format(Locale.US, "%.2f", monthlyPrice)}",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = "/mes",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
