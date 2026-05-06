package com.pietropuluche.veciapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.ProfileResponse
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.requestCurrentLocation
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import java.util.Locale

private data class ProfilePlanVisual(
    val label: String,
    val subtitle: String,
    val chipBackground: Color,
    val chipTextColor: Color,
    val isFamily: Boolean = false
)

@Composable
fun ProfileScreen(
    profile: ProfileResponse?,
    familyPreview: List<FamilyMapMemberResponse>,
    successMessage: String,
    errorMessage: String,
    onSaveProfile: (String, String) -> Unit,
    onUpdateLocation: (Double, Double, String, String) -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenFamily: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var localMessage by rememberSaveable { mutableStateOf("") }
    var editableEmail by rememberSaveable(profile?.userId, profile?.email) {
        mutableStateOf(profile?.email.orEmpty())
    }
    var editablePhone by rememberSaveable(profile?.userId, profile?.phone) {
        mutableStateOf(profile?.phone.orEmpty())
    }

    fun fetchLocation() {
        requestCurrentLocation(
            context = context,
            onLocation = { lat, lon ->
                onUpdateLocation(
                    lat,
                    lon,
                    profile?.district.orEmpty(),
                    profile?.city.orEmpty()
                )
                localMessage = "Ubicacion actualizada correctamente."
            },
            onError = { message ->
                localMessage = message
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            fetchLocation()
        } else {
            localMessage = "Necesitamos permiso de ubicacion para configurarla."
        }
    }

    val planVisual = profilePlanVisual(profile?.subscriptionPlan)
    val addressText = resolveProfileAddress(profile)
    val normalizedEmail = editableEmail.trim()
    val normalizedPhone = editablePhone.trim()
    val hasProfileChanges = profile != null &&
        (normalizedEmail != profile.email || normalizedPhone != profile.phone)

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
                    text = "Mi Perfil",
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(DeepOcean, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = "Perfil",
                        tint = SurfaceCard,
                        modifier = Modifier.size(42.dp)
                    )
                }
                PlanChip(
                    text = planVisual.label,
                    background = planVisual.chipBackground,
                    textColor = planVisual.chipTextColor
                )
                Text(
                    text = planVisual.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        item {
            if (errorMessage.isNotBlank()) {
                InlineMessage(errorMessage, true)
            }
            if (successMessage.isNotBlank()) {
                InlineMessage(successMessage, false)
            }
            if (localMessage.isNotBlank()) {
                InlineMessage(localMessage, false)
            }
        }
        if (planVisual.isFamily) {
            item {
                ActionBannerCard(
                    background = Color(0xFFF18A00),
                    iconLetter = "M",
                    title = "Mapa Familiar",
                    subtitle = "Ver ubicacion de tu familia",
                    trailingContent = {
                        FamilyPreviewBadges(familyPreview = familyPreview)
                    },
                    onClick = onOpenFamily
                )
            }
        }
        item {
            ActionBannerCard(
                background = DeepOcean,
                iconLetter = "S",
                title = "Gestionar Suscripcion",
                subtitle = "Ver planes y beneficios",
                onClick = onOpenSubscription
            )
        }
        item {
            SectionTitle("Informacion de contacto")
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    InfoRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = DeepOcean,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "Correo",
                        value = profile?.email.orEmpty()
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFDCE5F1)
                    )
                    InfoRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = DeepOcean,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = "Direccion",
                        value = addressText,
                        emphasize = addressText == "Sin configurar",
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
                        }
                    )
                }
            }
        }
        item {
            SectionTitle("Datos personales")
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Puedes actualizar tu celular y correo. Nombre, apellido y DNI se muestran como referencia y no se editan desde aqui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    ProfileReadOnlyField(
                        label = "Nombre",
                        value = profile?.firstName.orEmpty()
                    )
                    ProfileReadOnlyField(
                        label = "Apellido",
                        value = profile?.lastName.orEmpty()
                    )
                    ProfileReadOnlyField(
                        label = "DNI",
                        value = profile?.documentNumber?.ifBlank { "No registrado" } ?: "No registrado"
                    )
                    ProfileEditableField(
                        label = "Correo",
                        value = editableEmail,
                        onValueChange = { editableEmail = it },
                        keyboardType = KeyboardType.Email
                    )
                    ProfileEditableField(
                        label = "Celular",
                        value = editablePhone,
                        onValueChange = { editablePhone = it },
                        keyboardType = KeyboardType.Phone
                    )
                    Button(
                        onClick = {
                            when {
                                normalizedEmail.isBlank() || normalizedPhone.isBlank() -> {
                                    localMessage = "Correo y celular son obligatorios."
                                }

                                !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                                    localMessage = "Ingresa un correo valido."
                                }

                                else -> {
                                    localMessage = ""
                                    onSaveProfile(normalizedEmail, normalizedPhone)
                                }
                            }
                        },
                        enabled = hasProfileChanges && normalizedEmail.isNotBlank() && normalizedPhone.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepOcean)
                    ) {
                        Text(
                            text = "Guardar cambios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        item {
            SectionTitle("Preferencias")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreferenceRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = DeepOcean,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    title = "Notificaciones",
                    onClick = { localMessage = "Configuracion disponible pronto." }
                )
                if (!planVisual.isFamily) {
                    PreferenceRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = DeepOcean,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        title = "Privacidad",
                        onClick = { localMessage = "Configuracion disponible pronto." }
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = Color(0xFFE45C5C),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "  Cerrar sesion",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE45C5C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun ProfileReadOnlyField(
    label: String,
    value: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        colors = profileFieldColors()
    )
}

@Composable
private fun ProfileEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = profileFieldColors()
    )
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF8FBFF),
    unfocusedContainerColor = Color(0xFFF8FBFF),
    disabledContainerColor = Color(0xFFF8FBFF),
    focusedBorderColor = Color(0xFFD7E1EF),
    unfocusedBorderColor = Color(0xFFD7E1EF),
    disabledBorderColor = Color(0xFFD7E1EF),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextPrimary,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
    disabledLabelColor = TextSecondary
)

@Composable
private fun PlanChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActionBannerCard(
    background: Color,
    iconLetter: String,
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconLetter,
                        color = SurfaceCard,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SurfaceCard,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfaceCard.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailingContent?.invoke()
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SurfaceCard
                )
            }
        }
    }
}

@Composable
private fun FamilyPreviewBadges(
    familyPreview: List<FamilyMapMemberResponse>
) {
    val preview = familyPreview.take(3)
    val palette = listOf(
        Color(0xFFFF5EB4),
        Color(0xFF34B6FF),
        Color(0xFF2FCF77)
    )

    Box(modifier = Modifier.height(28.dp)) {
        preview.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .offset(x = (index * 18).dp)
                    .size(28.dp)
                    .background(palette[index % palette.size], CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.alias?.take(1)?.uppercase(Locale.getDefault())
                        ?: member.fullName.take(1).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = SurfaceCard,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        val extraCount = familyPreview.size - preview.size
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (preview.size * 18).dp)
                    .size(28.dp)
                    .background(Color(0xFF2FCF77), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$extraCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = SurfaceCard,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    emphasize: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PreferenceRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

private fun profilePlanVisual(plan: String?): ProfilePlanVisual {
    return when (plan?.uppercase(Locale.getDefault())) {
        "PREMIUM" -> ProfilePlanVisual(
            label = "Plan Premium",
            subtitle = "Prioridad alta en reportes",
            chipBackground = DeepOcean,
            chipTextColor = SurfaceCard
        )

        "FAMILY" -> ProfilePlanVisual(
            label = "Plan Familiar",
            subtitle = "Prioridad maxima en reportes",
            chipBackground = Color(0xFFFFA31A),
            chipTextColor = SurfaceCard,
            isFamily = true
        )

        else -> ProfilePlanVisual(
            label = "Plan Basico",
            subtitle = "Prioridad baja en reportes",
            chipBackground = Color(0xFFE6E8EC),
            chipTextColor = TextSecondary
        )
    }
}

private fun resolveProfileAddress(profile: ProfileResponse?): String {
    if (profile == null) return "Sin configurar"
    val districtCity = listOfNotNull(
        profile.district?.takeIf { it.isNotBlank() },
        profile.city?.takeIf { it.isNotBlank() }
    )
    if (districtCity.isNotEmpty()) {
        return districtCity.joinToString(", ")
    }
    if (profile.currentLatitude != null && profile.currentLongitude != null) {
        return "${profile.currentLatitude}, ${profile.currentLongitude}"
    }
    return "Sin configurar"
}
