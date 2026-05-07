package com.pietropuluche.veciapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.SendToMobile
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.FamilyInvitationResponse
import com.pietropuluche.veciapp.data.model.FamilyMemberResponse
import com.pietropuluche.veciapp.ui.common.EmptyState
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.resolveAddressReference
import com.pietropuluche.veciapp.ui.theme.AlertAmber
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.SuccessGreen
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private data class FamilyUiMember(
    val userId: Long,
    val displayName: String,
    val shortLabel: String,
    val roleLabel: String,
    val groupType: String,
    val phone: String?,
    val latitude: Double?,
    val longitude: Double?,
    val district: String?,
    val city: String?,
    val isOwner: Boolean,
    val isOnline: Boolean,
    val updatedLabel: String,
    val batteryPercent: Int,
    val markerColor: Color,
    val xFraction: Float,
    val yFraction: Float
)

private val familyMarkerColors = listOf(
    Color(0xFFFF48A8),
    Color(0xFF3387FF),
    Color(0xFF20C85A),
    Color(0xFFA14DFF),
    Color(0xFFF18A00)
)

private const val GroupFamily = "FAMILY"
private const val GroupOther = "OTHER"

@Composable
fun FamilyScreen(
    currentUserId: Long?,
    currentPlan: String?,
    familyMembers: List<FamilyMemberResponse>,
    familyMap: List<FamilyMapMemberResponse>,
    invitations: List<FamilyInvitationResponse>,
    successMessage: String,
    errorMessage: String,
    onRefresh: () -> Unit,
    onAddMember: (String, String, String, String) -> Unit,
    onRemoveMember: (Long) -> Unit,
    onAcceptInvitation: (Long) -> Unit,
    onRejectInvitation: (Long) -> Unit,
    onLeaveGroup: () -> Unit,
    onOpenSubscription: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isFamilyPlan = currentPlan.equals("FAMILY", ignoreCase = true)
    val uiMembers = remember(familyMembers, familyMap) {
        buildFamilyUiMembers(familyMembers, familyMap)
    }
    val ownerMember = uiMembers.firstOrNull { it.isOwner }
    val familyCards = uiMembers.filterNot { it.isOwner }
    val groupedMapMembers = remember(familyCards) {
        familyCards.groupBy { normalizeGroupType(it.groupType) }
    }
    val groupedManagedMembers = remember(familyMembers) {
        familyMembers.groupBy { normalizeGroupType(it.groupType) }
    }
    val canAccessSharedGroup = familyMap.isNotEmpty()
    val canManageGroup = isFamilyPlan && ownerMember?.userId == currentUserId
    val isJoinedAsMember = canAccessSharedGroup && ownerMember?.userId != currentUserId
    var selectedMember by remember { mutableStateOf<FamilyUiMember?>(null) }
    var email by rememberSaveable { mutableStateOf("") }
    var alias by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf(GroupFamily) }

    fun openMapsFor(member: FamilyUiMember?) {
        val lat = member?.latitude ?: ownerMember?.latitude ?: return
        val lon = member?.longitude ?: ownerMember?.longitude ?: return
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
        )
        context.startActivity(intent)
    }

    fun openCall(phone: String?) {
        if (phone.isNullOrBlank()) return
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    fun openMessage(phone: String?) {
        if (phone.isNullOrBlank()) return
        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFE))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Mapa Familiar",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${familyCards.size} miembros",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Actualizar",
                                tint = TextSecondary
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary
                            )
                        }
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
            }

            if (invitations.isNotEmpty()) {
                item {
                    InvitationSection(
                        invitations = invitations,
                        onAcceptInvitation = onAcceptInvitation,
                        onRejectInvitation = onRejectInvitation
                    )
                }
            }

            if (!isFamilyPlan && !canAccessSharedGroup) {
                item {
                    LockedFamilyPlanCard(
                        onOpenSubscription = onOpenSubscription
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        FamilyLegendCard(modifier = Modifier.weight(1f))
                        OpenMapsButton(
                            enabled = ownerMember?.latitude != null && ownerMember.longitude != null,
                            onClick = { openMapsFor(ownerMember) }
                        )
                    }
                }
                item {
                    FamilyMapPanel(
                        members = uiMembers,
                        onMemberClick = { member ->
                            if (!member.isOwner) {
                                selectedMember = member
                            }
                        }
                    )
                }
                item {
                    Text(
                        text = "Miembros vinculados",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    if (familyCards.isEmpty()) {
                        EmptyState("Aun no has vinculado familiares a tu plan.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            GroupedMemberChipsSection(
                                title = "Grupo Familia",
                                members = groupedMapMembers[GroupFamily].orEmpty(),
                                onSelectMember = { selectedMember = it }
                            )
                            GroupedMemberChipsSection(
                                title = "Grupo Otro",
                                members = groupedMapMembers[GroupOther].orEmpty(),
                                onSelectMember = { selectedMember = it }
                            )
                        }
                    }
                }
                if (canManageGroup) {
                    item {
                        ManageFamilySection(
                            groupedMembers = groupedManagedMembers,
                            email = email,
                            alias = alias,
                            relationship = relationship,
                            selectedGroup = selectedGroup,
                            onEmailChange = { email = it },
                            onAliasChange = { alias = it },
                            onRelationshipChange = { relationship = it },
                            onSelectedGroupChange = { selectedGroup = it },
                            onAddMember = {
                                onAddMember(email, alias, relationship, selectedGroup)
                                email = ""
                                alias = ""
                                relationship = ""
                                selectedGroup = GroupFamily
                            },
                            onRemoveMember = onRemoveMember
                        )
                    }
                }
                if (isJoinedAsMember) {
                    item {
                        LeaveGroupCard(onLeaveGroup = onLeaveGroup)
                    }
                }
            }
        }

        selectedMember?.let { member ->
            FamilyMemberDetailDialog(
                member = member,
                onCall = { openCall(member.phone) },
                onMessage = { openMessage(member.phone) },
                onNavigate = { openMapsFor(member) },
                onClose = { selectedMember = null }
            )
        }
    }
}

@Composable
private fun InvitationSection(
    invitations: List<FamilyInvitationResponse>,
    onAcceptInvitation: (Long) -> Unit,
    onRejectInvitation: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Invitaciones pendientes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            invitations.forEach { invitation ->
                InvitationCard(
                    invitation = invitation,
                    onAccept = { onAcceptInvitation(invitation.id) },
                    onReject = { onRejectInvitation(invitation.id) }
                )
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: FamilyInvitationResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = invitation.ownerFullName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Te invita al ${groupLabel(invitation.groupType)}",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepOcean,
                fontWeight = FontWeight.SemiBold
            )
            val detail = listOfNotNull(
                invitation.alias?.takeIf { it.isNotBlank() }?.let { "Alias: $it" },
                invitation.relationshipLabel?.takeIf { it.isNotBlank() }?.let { "Relacion: $it" }
            ).joinToString("  ")
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepOcean,
                        contentColor = SurfaceCard
                    )
                ) {
                    Text("Aceptar", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFECEC),
                        contentColor = Color(0xFFE05B5B)
                    )
                ) {
                    Text("Rechazar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LockedFamilyPlanCard(
    onOpenSubscription: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(AlertAmber.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = AlertAmber,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "Mapa Familiar disponible con Plan Familiar",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Activa el plan Familiar para ver ubicacion, estado y acciones rapidas de los miembros de tu hogar.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onOpenSubscription,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepOcean,
                    contentColor = SurfaceCard
                )
            ) {
                Text(
                    text = "Ver Plan Familiar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LeaveGroupCard(
    onLeaveGroup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Salir del grupo",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Si ya no quieres compartir ubicacion con este grupo, puedes salir cuando quieras.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Button(
                onClick = onLeaveGroup,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE05B5B),
                    contentColor = SurfaceCard
                )
            ) {
                Text(
                    text = "Salir del grupo",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FamilyLegendCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Estado",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDotLabel("En linea", SuccessGreen)
                StatusDotLabel("Desconectado", Color(0xFFACB6C3))
            }
        }
    }
}

@Composable
private fun StatusDotLabel(
    text: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun OpenMapsButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceCard,
            contentColor = DeepOcean,
            disabledContainerColor = SurfaceCard,
            disabledContentColor = TextSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.NearMe,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = " Abrir Maps",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FamilyMapPanel(
    members: List<FamilyUiMember>,
    onMemberClick: (FamilyUiMember) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(470.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            FamilyMapGrid(
                modifier = Modifier.fillMaxSize()
            )

            members.forEach { member ->
                val markerX = maxWidth * member.xFraction - 20.dp
                val markerY = maxHeight * member.yFraction - 26.dp
                Column(
                    modifier = Modifier
                        .offset(x = markerX, y = markerY)
                        .clickable(enabled = !member.isOwner) { onMemberClick(member) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(if (member.isOwner) 18.dp else 42.dp)
                                .background(
                                    if (member.isOwner) DeepOcean else member.markerColor,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (member.isOwner) "" else member.shortLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = SurfaceCard,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(11.dp)
                                .background(
                                    if (member.isOnline) SuccessGreen else Color(0xFFD7DDE6),
                                    CircleShape
                                )
                        )
                    }
                    Text(
                        text = if (member.isOwner) "Tu" else member.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyMapGrid(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val background = Color(0xFFF9FBFE)
        val fineLine = Color(0xFFE9EFF6)
        val majorLine = Color(0xFFDDE6F2)
        drawRect(background)

        val minorStepX = size.width / 8f
        val minorStepY = size.height / 10f
        repeat(9) { index ->
            val x = index * minorStepX
            drawLine(
                color = if (index % 2 == 0) majorLine else fineLine,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (index % 2 == 0) 2f else 1f
            )
        }
        repeat(11) { index ->
            val y = index * minorStepY
            drawLine(
                color = if (index % 2 == 0) majorLine else fineLine,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = if (index % 2 == 0) 2f else 1f
            )
        }
    }
}

@Composable
private fun FamilyMemberChip(
    member: FamilyUiMember,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(member.markerColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.shortLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = SurfaceCard,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(10.dp)
                        .background(
                            if (member.isOnline) SuccessGreen else Color(0xFFD7DDE6),
                            CircleShape
                        )
                )
            }
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = member.updatedLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GroupedMemberChipsSection(
    title: String,
    members: List<FamilyUiMember>,
    onSelectMember: (FamilyUiMember) -> Unit
) {
    if (members.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(members.size) { index ->
                val member = members[index]
                FamilyMemberChip(
                    member = member,
                    onClick = { onSelectMember(member) }
                )
            }
        }
    }
}

@Composable
private fun ManageFamilySection(
    groupedMembers: Map<String, List<FamilyMemberResponse>>,
    email: String,
    alias: String,
    relationship: String,
    selectedGroup: String,
    onEmailChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onSelectedGroupChange: (String) -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Gestionar miembros",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige si el nuevo integrante se guarda en Familia o en Otro.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GroupOptionCard(
                    title = "Familia",
                    selected = normalizeGroupType(selectedGroup) == GroupFamily,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectedGroupChange(GroupFamily) }
                )
                GroupOptionCard(
                    title = "Otro",
                    selected = normalizeGroupType(selectedGroup) == GroupOther,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectedGroupChange(GroupOther) }
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Correo del integrante") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = alias,
                    onValueChange = onAliasChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Alias") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = onRelationshipChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Relacion") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            Button(
                onClick = onAddMember,
                enabled = email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepOcean,
                    contentColor = SurfaceCard
                )
            ) {
                Text(
                    text = "Enviar invitacion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val familyGroupMembers = groupedMembers[GroupFamily].orEmpty()
            val otherGroupMembers = groupedMembers[GroupOther].orEmpty()
            if (familyGroupMembers.isNotEmpty() || otherGroupMembers.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFE7ECF4))
                GroupedManagedMembersSection(
                    title = "Grupo Familia",
                    members = familyGroupMembers,
                    onRemoveMember = onRemoveMember
                )
                GroupedManagedMembersSection(
                    title = "Grupo Otro",
                    members = otherGroupMembers,
                    onRemoveMember = onRemoveMember
                )
            }
        }
    }
}

@Composable
private fun GroupOptionCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) DeepOcean.copy(alpha = 0.08f) else Color(0xFFF5F8FD)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) DeepOcean else Color(0xFFDCE5F1)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) DeepOcean else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GroupedManagedMembersSection(
    title: String,
    members: List<FamilyMemberResponse>,
    onRemoveMember: (Long) -> Unit
) {
    if (members.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        members.forEach { member ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = member.alias ?: member.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = member.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "Quitar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE05B5B),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onRemoveMember(member.id) }
                )
            }
        }
    }
}

@Composable
private fun FamilyMemberDetailDialog(
    member: FamilyUiMember,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onNavigate: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var resolvedPlaceName by rememberSaveable(
        member.userId,
        member.latitude,
        member.longitude,
        member.district,
        member.city
    ) {
        mutableStateOf(memberPlaceName(member))
    }
    val coordinatesText = memberCoordinatesText(member)

    LaunchedEffect(member.userId, member.latitude, member.longitude) {
        resolvedPlaceName = memberPlaceName(member)
        val latitude = member.latitude
        val longitude = member.longitude
        if (latitude != null && longitude != null) {
            resolveAddressReference(context, latitude, longitude)?.let { resolved ->
                resolvedPlaceName = resolved
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(member.markerColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.shortLabel,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = SurfaceCard,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .size(12.dp)
                                        .background(
                                            if (member.isOnline) SuccessGreen else Color(0xFFD7DDE6),
                                            CircleShape
                                        )
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = member.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = groupLabel(member.groupType),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepOcean,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Actualizado: ${member.updatedLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bateria del dispositivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${member.batteryPercent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { member.batteryPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = SuccessGreen,
                                trackColor = Color(0xFFDDE6F4)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ubicacion",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = coordinatesText ?: "Sin coordenadas compartidas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            resolvedPlaceName?.takeIf { it.isNotBlank() }?.let { place ->
                                Text(
                                    text = place,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailActionButton(
                            title = "Llamar",
                            icon = Icons.Outlined.Call,
                            background = Color(0xFF13C655),
                            modifier = Modifier.weight(1f),
                            enabled = !member.phone.isNullOrBlank(),
                            onClick = onCall
                        )
                        DetailActionButton(
                            title = "Mensaje",
                            icon = Icons.AutoMirrored.Outlined.SendToMobile,
                            background = Color(0xFF3A7BFF),
                            modifier = Modifier.weight(1f),
                            enabled = !member.phone.isNullOrBlank(),
                            onClick = onMessage
                        )
                        DetailActionButton(
                            title = "Navegar",
                            icon = Icons.Outlined.NearMe,
                            background = DeepOcean,
                            modifier = Modifier.weight(1f),
                            enabled = member.latitude != null && member.longitude != null,
                            onClick = onNavigate
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE7ECF4))
                    Text(
                        text = "Cerrar",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onClose)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = SurfaceCard,
            disabledContainerColor = background.copy(alpha = 0.3f),
            disabledContentColor = SurfaceCard.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun buildFamilyUiMembers(
    familyMembers: List<FamilyMemberResponse>,
    familyMap: List<FamilyMapMemberResponse>
): List<FamilyUiMember> {
    if (familyMap.isEmpty()) return emptyList()

    val memberByUserId = familyMembers.associateBy { it.memberUserId }
    val owner = familyMap.firstOrNull()
    val ownerLat = owner?.latitude
    val ownerLon = owner?.longitude

    val relativeMembers = familyMap.mapIndexed { index, member ->
        val linked = memberByUserId[member.userId]
        val isOwner = index == 0
        val isOnline = member.latitude != null && member.longitude != null
        val updatedLabel = if (isOwner) {
            "Hace 1 min"
        } else {
            simulatedUpdatedLabel(member.userId, isOnline)
        }
        val battery = simulatedBatteryPercent(member.userId, isOnline, isOwner)
        val point = resolveFamilyPoint(
            index = index,
            isOwner = isOwner,
            memberLatitude = member.latitude,
            memberLongitude = member.longitude,
            ownerLatitude = ownerLat,
            ownerLongitude = ownerLon
        )
        FamilyUiMember(
            userId = member.userId,
            displayName = when {
                isOwner -> "Tu"
                !member.alias.isNullOrBlank() -> member.alias
                !linked?.alias.isNullOrBlank() -> linked?.alias ?: member.fullName
                else -> member.fullName
            },
            shortLabel = when {
                isOwner -> ""
                !member.alias.isNullOrBlank() -> member.alias.take(1).uppercase(Locale.getDefault())
                !linked?.alias.isNullOrBlank() -> linked?.alias?.take(1)?.uppercase(Locale.getDefault()).orEmpty()
                else -> member.fullName.take(1).uppercase(Locale.getDefault())
            },
            roleLabel = when {
                isOwner -> "Titular"
                !member.relationshipLabel.isNullOrBlank() -> member.relationshipLabel
                !linked?.relationshipLabel.isNullOrBlank() -> linked?.relationshipLabel ?: "Familiar"
                else -> "Familiar"
            },
            groupType = linked?.groupType ?: GroupFamily,
            phone = linked?.phone,
            latitude = member.latitude,
            longitude = member.longitude,
            district = member.district,
            city = member.city,
            isOwner = isOwner,
            isOnline = isOnline,
            updatedLabel = updatedLabel,
            batteryPercent = battery,
            markerColor = if (isOwner) DeepOcean else familyMarkerColors[(index - 1).mod(familyMarkerColors.size)],
            xFraction = point.first,
            yFraction = point.second
        )
    }
    return relativeMembers
}

private fun resolveFamilyPoint(
    index: Int,
    isOwner: Boolean,
    memberLatitude: Double?,
    memberLongitude: Double?,
    ownerLatitude: Double?,
    ownerLongitude: Double?
): Pair<Float, Float> {
    if (isOwner) {
        return 0.5f to 0.46f
    }
    if (memberLatitude != null && memberLongitude != null && ownerLatitude != null && ownerLongitude != null) {
        val dx = memberLongitude - ownerLongitude
        val dy = memberLatitude - ownerLatitude
        val scaleX = max(abs(dx), 0.004)
        val scaleY = max(abs(dy), 0.004)
        val x = (0.5f + (dx / (scaleX * 5.0)).toFloat()).coerceIn(0.18f, 0.82f)
        val y = (0.46f - (dy / (scaleY * 5.0)).toFloat()).coerceIn(0.18f, 0.82f)
        return x to y
    }
    val fallback = listOf(
        0.27f to 0.22f,
        0.72f to 0.28f,
        0.24f to 0.64f,
        0.70f to 0.68f,
        0.50f to 0.20f
    )
    return fallback[(index - 1).mod(fallback.size)]
}

private fun normalizeGroupType(groupType: String?): String {
    return if (groupType.equals(GroupOther, ignoreCase = true)) {
        GroupOther
    } else {
        GroupFamily
    }
}

private fun groupLabel(groupType: String?): String {
    return if (normalizeGroupType(groupType) == GroupOther) {
        "Grupo Otro"
    } else {
        "Grupo Familia"
    }
}

private fun simulatedBatteryPercent(
    userId: Long,
    isOnline: Boolean,
    isOwner: Boolean
): Int {
    if (!isOnline) return 32 + (userId % 18).toInt()
    if (isOwner) return 88
    return 62 + (userId % 28).toInt()
}

private fun simulatedUpdatedLabel(
    userId: Long,
    isOnline: Boolean
): String {
    if (!isOnline) return "Sin conexion"
    val minutes = 1 + (userId % 15).toInt()
    return "Hace $minutes min"
}

private fun memberCoordinatesText(member: FamilyUiMember): String? {
    if (member.latitude == null || member.longitude == null) {
        return null
    }
    return "${String.format(Locale.US, "%.6f", member.latitude)}, ${String.format(Locale.US, "%.6f", member.longitude)}"
}

private fun memberPlaceName(member: FamilyUiMember): String? {
    val place = listOfNotNull(
        member.district?.takeIf { it.isNotBlank() },
        member.city?.takeIf { it.isNotBlank() }
    ).joinToString(", ")
    return place.ifBlank { null }
}
