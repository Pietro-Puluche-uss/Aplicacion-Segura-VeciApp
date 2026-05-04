package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pietropuluche.veciapp.data.model.SubscriptionPlanResponse
import com.pietropuluche.veciapp.data.model.UserSubscriptionResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.theme.TextSecondary

@Composable
fun SubscriptionScreen(
    currentSubscription: UserSubscriptionResponse?,
    plans: List<SubscriptionPlanResponse>,
    successMessage: String,
    errorMessage: String,
    onSelectPlan: (String) -> Unit
) {
    ScreenContainer(
        title = "Suscripcion",
        subtitle = "Cambia el plan de tu cuenta segun el uso que necesitas."
    ) {
        SectionCard {
            InlineMessage(errorMessage, true)
            InlineMessage(successMessage, false)
            Text("Plan actual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(currentSubscription?.currentPlan ?: "-", color = MaterialTheme.colorScheme.primary)
            Text(currentSubscription?.status ?: "-", color = TextSecondary)
        }

        plans.forEach { plan ->
            SectionCard {
                Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("S/ ${plan.monthlyPrice}", color = MaterialTheme.colorScheme.primary)
                plan.features.forEach { feature ->
                    Text("- $feature", color = TextSecondary)
                }
                Button(onClick = { onSelectPlan(plan.code) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Elegir ${plan.name}")
                }
            }
        }
    }
}
