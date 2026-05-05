package com.pietropuluche.veciapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pietropuluche.veciapp.data.model.CreateEmergencyRequest
import com.pietropuluche.veciapp.data.model.CreateIncidentReportRequest
import com.pietropuluche.veciapp.data.model.DashboardHomeResponse
import com.pietropuluche.veciapp.data.model.EmergencyResponse
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.FamilyMemberRequest
import com.pietropuluche.veciapp.data.model.FamilyMemberResponse
import com.pietropuluche.veciapp.data.model.HistoryItemResponse
import com.pietropuluche.veciapp.data.model.IncidentReportResponse
import com.pietropuluche.veciapp.data.model.ProfileResponse
import com.pietropuluche.veciapp.data.model.ReportCategoryResponse
import com.pietropuluche.veciapp.data.model.SubscriptionPlanResponse
import com.pietropuluche.veciapp.data.model.UpdateLocationRequest
import com.pietropuluche.veciapp.data.model.UpdateProfileRequest
import com.pietropuluche.veciapp.data.model.UpdateSubscriptionRequest
import com.pietropuluche.veciapp.data.model.UserSubscriptionResponse
import com.pietropuluche.veciapp.data.repository.VeciAppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VeciAppUiState(
    val isLoading: Boolean = false,
    val dashboard: DashboardHomeResponse? = null,
    val profile: ProfileResponse? = null,
    val categories: List<ReportCategoryResponse> = emptyList(),
    val reports: List<IncidentReportResponse> = emptyList(),
    val emergencies: List<EmergencyResponse> = emptyList(),
    val history: List<HistoryItemResponse> = emptyList(),
    val plans: List<SubscriptionPlanResponse> = emptyList(),
    val subscription: UserSubscriptionResponse? = null,
    val familyMembers: List<FamilyMemberResponse> = emptyList(),
    val familyMap: List<FamilyMapMemberResponse> = emptyList(),
    val successMessage: String = "",
    val errorMessage: String = ""
)

class VeciAppViewModel(
    private val repository: VeciAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeciAppUiState())
    val uiState: StateFlow<VeciAppUiState> = _uiState.asStateFlow()

    fun bootstrap(clearFeedback: Boolean = true) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = if (clearFeedback) "" else currentState.errorMessage,
                successMessage = if (clearFeedback) "" else currentState.successMessage
            )
            val profileDeferred = async { repository.getProfile() }
            val dashboardDeferred = async { repository.getDashboard() }
            val categoriesDeferred = async { repository.getReportCategories() }
            val subscriptionDeferred = async { repository.getMySubscription() }
            val plansDeferred = async { repository.getSubscriptionPlans() }
            val historyDeferred = async { repository.getMyHistory() }
            val reportsDeferred = async { repository.getMyReports() }
            val emergenciesDeferred = async { repository.getMyEmergencies() }
            val membersDeferred = async { repository.getFamilyMembers() }
            val familyMapDeferred = async { repository.getFamilyMap() }

            val profileResult = profileDeferred.await()
            val dashboardResult = dashboardDeferred.await()
            val categoriesResult = categoriesDeferred.await()
            val subscriptionResult = subscriptionDeferred.await()
            val plansResult = plansDeferred.await()
            val historyResult = historyDeferred.await()
            val reportsResult = reportsDeferred.await()
            val emergenciesResult = emergenciesDeferred.await()
            val membersResult = membersDeferred.await()
            val familyMapResult = familyMapDeferred.await()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = profileResult.getOrNull() ?: currentState.profile,
                dashboard = dashboardResult.getOrNull() ?: currentState.dashboard,
                categories = categoriesResult.getOrElse { currentState.categories },
                subscription = subscriptionResult.getOrNull() ?: currentState.subscription,
                plans = plansResult.getOrElse { currentState.plans },
                history = historyResult.getOrElse { currentState.history },
                reports = reportsResult.getOrElse { currentState.reports },
                emergencies = emergenciesResult.getOrElse { currentState.emergencies },
                familyMembers = membersResult.getOrElse { currentState.familyMembers },
                familyMap = familyMapResult.getOrElse { currentState.familyMap },
                errorMessage = listOf(
                    profileResult.exceptionOrNull()?.message,
                    dashboardResult.exceptionOrNull()?.message,
                    categoriesResult.exceptionOrNull()?.message,
                    subscriptionResult.exceptionOrNull()?.message,
                    plansResult.exceptionOrNull()?.message,
                    historyResult.exceptionOrNull()?.message,
                    reportsResult.exceptionOrNull()?.message,
                    emergenciesResult.exceptionOrNull()?.message,
                    membersResult.exceptionOrNull()?.message,
                    familyMapResult.exceptionOrNull()?.message
                ).firstOrNull { !it.isNullOrBlank() }.orEmpty()
            )
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            repository.getMyHistory().onSuccess {
                _uiState.value = _uiState.value.copy(history = it)
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun refreshFamily() {
        viewModelScope.launch {
            val membersResult = repository.getFamilyMembers()
            val mapResult = repository.getFamilyMap()
            _uiState.value = _uiState.value.copy(
                familyMembers = membersResult.getOrDefault(emptyList()),
                familyMap = mapResult.getOrDefault(emptyList()),
                errorMessage = membersResult.exceptionOrNull()?.message
                    ?: mapResult.exceptionOrNull()?.message
                    ?: ""
            )
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        documentNumber: String,
        profilePhotoUrl: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(
                UpdateProfileRequest(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = phone.trim(),
                    documentNumber = documentNumber.trim().ifBlank { null },
                    profilePhotoUrl = profilePhotoUrl.trim().ifBlank { null }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(profile = it, successMessage = "Perfil actualizado", errorMessage = "")
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, district: String, city: String) {
        viewModelScope.launch {
            repository.updateLocation(
                UpdateLocationRequest(
                    latitude = latitude,
                    longitude = longitude,
                    district = district.ifBlank { null },
                    city = city.ifBlank { null }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(profile = it, successMessage = "Ubicacion actualizada", errorMessage = "")
                refreshFamily()
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun createEmergency(
        type: String,
        latitude: Double?,
        longitude: Double?,
        address: String,
        notes: String,
        evidenceImageBase64: String?
    ) {
        viewModelScope.launch {
            repository.createEmergency(
                CreateEmergencyRequest(
                    type = type,
                    latitude = latitude,
                    longitude = longitude,
                    addressReference = address.ifBlank { null },
                    notes = notes.ifBlank { null },
                    evidenceImageBase64 = evidenceImageBase64?.ifBlank { null }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    emergencies = listOf(it) + _uiState.value.emergencies,
                    history = _uiState.value.history,
                    successMessage = "Alerta enviada correctamente",
                    errorMessage = ""
                )
                bootstrap(clearFeedback = false)
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun createReport(
        category: String,
        title: String,
        description: String,
        addressReference: String,
        latitude: Double?,
        longitude: Double?,
        evidenceImageBase64: String?
    ) {
        viewModelScope.launch {
            repository.createReport(
                CreateIncidentReportRequest(
                    category = category,
                    title = title.trim(),
                    description = description.trim(),
                    addressReference = addressReference.ifBlank { null },
                    latitude = latitude,
                    longitude = longitude,
                    evidenceImageBase64 = evidenceImageBase64?.ifBlank { null }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    reports = listOf(it) + _uiState.value.reports,
                    successMessage = "Reporte enviado correctamente",
                    errorMessage = ""
                )
                bootstrap(clearFeedback = false)
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun updateSubscription(plan: String) {
        viewModelScope.launch {
            repository.updateSubscription(UpdateSubscriptionRequest(plan))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(subscription = it, successMessage = "Plan actualizado", errorMessage = "")
                    bootstrap(clearFeedback = false)
                }.onFailure { error ->
                    showError(error.message.orEmpty())
                }
        }
    }

    fun addFamilyMember(email: String, alias: String, relationshipLabel: String) {
        viewModelScope.launch {
            repository.addFamilyMember(
                FamilyMemberRequest(
                    email = email.trim(),
                    alias = alias.trim().ifBlank { null },
                    relationshipLabel = relationshipLabel.trim().ifBlank { null }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "Miembro agregado", errorMessage = "")
                refreshFamily()
            }.onFailure { error ->
                showError(error.message.orEmpty())
            }
        }
    }

    fun removeFamilyMember(id: Long) {
        viewModelScope.launch {
            repository.removeFamilyMember(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = it, errorMessage = "")
                    refreshFamily()
                }.onFailure { error ->
                    showError(error.message.orEmpty())
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = "", errorMessage = "")
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, successMessage = "")
    }
}
