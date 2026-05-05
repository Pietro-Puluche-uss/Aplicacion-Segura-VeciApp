package com.pietropuluche.veciapp.data.model

data class ApiMessageResponse(
    val message: String
)

data class ErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val errorCode: String? = null
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val documentNumber: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val userId: Long,
    val fullName: String,
    val token: String,
    val subscriptionPlan: String
)

data class ProfileResponse(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val documentNumber: String? = null,
    val profilePhotoUrl: String? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val district: String? = null,
    val city: String? = null,
    val subscriptionPlan: String,
    val subscriptionStatus: String,
    val subscriptionActivatedAt: String? = null,
    val createdAt: String? = null
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val documentNumber: String? = null,
    val profilePhotoUrl: String? = null
)

data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double,
    val district: String? = null,
    val city: String? = null
)

data class DashboardHomeResponse(
    val nearbyAuthorities: Int,
    val reportsToday: Long,
    val subscriptionPlan: String
)

data class ReportCategoryResponse(
    val id: String,
    val label: String
)

data class CreateEmergencyRequest(
    val type: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressReference: String? = null,
    val notes: String? = null
)

data class EmergencyResponse(
    val id: Long,
    val type: String,
    val typeLabel: String,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressReference: String? = null,
    val notes: String? = null,
    val assignedAuthorityName: String? = null,
    val assignedDistanceKm: Double? = null,
    val estimatedResponseMinutes: Int? = null,
    val createdAt: String
)

data class CreateIncidentReportRequest(
    val category: String,
    val title: String,
    val description: String,
    val addressReference: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class IncidentReportResponse(
    val id: Long,
    val category: String,
    val categoryLabel: String,
    val status: String,
    val title: String,
    val description: String,
    val addressReference: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String
)

data class HistoryItemResponse(
    val itemType: String,
    val itemId: Long,
    val title: String,
    val subtitle: String,
    val status: String,
    val location: String? = null,
    val createdAt: String? = null
)

data class SubscriptionPlanResponse(
    val code: String,
    val name: String,
    val monthlyPrice: Double,
    val features: List<String>
)

data class UserSubscriptionResponse(
    val currentPlan: String,
    val status: String,
    val activatedAt: String? = null
)

data class UpdateSubscriptionRequest(
    val plan: String
)

data class FamilyMemberRequest(
    val email: String,
    val alias: String? = null,
    val relationshipLabel: String? = null
)

data class FamilyMemberResponse(
    val id: Long,
    val memberUserId: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val alias: String? = null,
    val relationshipLabel: String? = null,
    val createdAt: String
)

data class FamilyMapMemberResponse(
    val userId: Long,
    val fullName: String,
    val alias: String? = null,
    val relationshipLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val district: String? = null,
    val city: String? = null,
    val profilePhotoUrl: String? = null
)

data class UiOption(
    val id: String,
    val label: String
)

val emergencyTypeOptions = listOf(
    UiOption("ROBBERY", "Robo/Asalto"),
    UiOption("VIOLENCE", "Violencia"),
    UiOption("ACCIDENT", "Accidente"),
    UiOption("MISSING_PERSON", "Persona desaparecida"),
    UiOption("THREAT", "Amenaza"),
    UiOption("OTHER", "Otro")
)
