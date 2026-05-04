package com.pietropuluche.veciapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.FamilyMemberResponse
import com.pietropuluche.veciapp.ui.common.ClickableInfoRow
import com.pietropuluche.veciapp.ui.common.EmptyState
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.theme.TextSecondary

@Composable
fun FamilyScreen(
    familyMembers: List<FamilyMemberResponse>,
    familyMap: List<FamilyMapMemberResponse>,
    successMessage: String,
    errorMessage: String,
    onAddMember: (String, String, String) -> Unit,
    onRemoveMember: (Long) -> Unit
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var alias by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf("") }

    ScreenContainer(
        title = "Familia",
        subtitle = "Administra los miembros vinculados y revisa su ubicacion reportada."
    ) {
        SectionCard {
            InlineMessage(errorMessage, true)
            InlineMessage(successMessage, false)
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Correo del familiar") })
            OutlinedTextField(alias, { alias = it }, Modifier.fillMaxWidth(), label = { Text("Alias") })
            OutlinedTextField(relationship, { relationship = it }, Modifier.fillMaxWidth(), label = { Text("Relacion") })
            Button(
                onClick = {
                    onAddMember(email, alias, relationship)
                    email = ""
                    alias = ""
                    relationship = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank()
            ) {
                Text("Agregar miembro")
            }
        }

        SectionCard {
            Text("Miembros vinculados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (familyMembers.isEmpty()) {
                EmptyState("Todavia no tienes miembros familiares agregados.")
            } else {
                familyMembers.forEach { member ->
                    Text(member.fullName, fontWeight = FontWeight.SemiBold)
                    Text(member.email, color = TextSecondary)
                    Text(member.relationshipLabel ?: member.alias.orEmpty(), color = TextSecondary)
                    Button(onClick = { onRemoveMember(member.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Quitar miembro")
                    }
                }
            }
        }

        SectionCard {
            Text("Mapa familiar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (familyMap.isEmpty()) {
                EmptyState("No hay ubicaciones compartidas todavia.")
            } else {
                familyMap.forEach { member ->
                    val place = listOfNotNull(member.district, member.city).joinToString(", ").ifBlank { "Sin referencia" }
                    ClickableInfoRow(
                        title = member.alias ?: member.fullName,
                        subtitle = "$place - ${member.latitude ?: "-"}, ${member.longitude ?: "-"}"
                    ) {
                        val lat = member.latitude ?: return@ClickableInfoRow
                        val lon = member.longitude ?: return@ClickableInfoRow
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                        )
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
