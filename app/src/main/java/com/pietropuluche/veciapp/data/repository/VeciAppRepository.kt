package com.pietropuluche.veciapp.data.repository

import com.google.gson.Gson
import com.pietropuluche.veciapp.data.model.AuthResponse
import com.pietropuluche.veciapp.data.model.CreateEmergencyRequest
import com.pietropuluche.veciapp.data.model.CreateIncidentReportRequest
import com.pietropuluche.veciapp.data.model.DashboardHomeResponse
import com.pietropuluche.veciapp.data.model.EmergencyResponse
import com.pietropuluche.veciapp.data.model.ErrorResponse
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.FamilyMemberRequest
import com.pietropuluche.veciapp.data.model.FamilyMemberResponse
import com.pietropuluche.veciapp.data.model.HistoryItemResponse
import com.pietropuluche.veciapp.data.model.IncidentReportResponse
import com.pietropuluche.veciapp.data.model.LoginRequest
import com.pietropuluche.veciapp.data.model.ProfileResponse
import com.pietropuluche.veciapp.data.model.RegisterRequest
import com.pietropuluche.veciapp.data.model.ReportCategoryResponse
import com.pietropuluche.veciapp.data.model.SubscriptionPlanResponse
import com.pietropuluche.veciapp.data.model.UpdateLocationRequest
import com.pietropuluche.veciapp.data.model.UpdateProfileRequest
import com.pietropuluche.veciapp.data.model.UpdateSubscriptionRequest
import com.pietropuluche.veciapp.data.model.UserSubscriptionResponse
import com.pietropuluche.veciapp.data.remote.ApiService
import retrofit2.HttpException

class VeciAppRepository(
    private val apiService: ApiService
) {

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = runCatchingApi {
        apiService.register(request)
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = runCatchingApi {
        apiService.login(request)
    }

    suspend fun getProfile(): Result<ProfileResponse> = runCatchingApi {
        apiService.getProfile()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileResponse> = runCatchingApi {
        apiService.updateProfile(request)
    }

    suspend fun updateLocation(request: UpdateLocationRequest): Result<ProfileResponse> = runCatchingApi {
        apiService.updateLocation(request)
    }

    suspend fun getDashboard(): Result<DashboardHomeResponse> = runCatchingApi {
        apiService.getDashboard()
    }

    suspend fun getReportCategories(): Result<List<ReportCategoryResponse>> = runCatchingApi {
        apiService.getReportCategories()
    }

    suspend fun createEmergency(request: CreateEmergencyRequest): Result<EmergencyResponse> = runCatchingApi {
        apiService.createEmergency(request)
    }

    suspend fun getMyEmergencies(): Result<List<EmergencyResponse>> = runCatchingApi {
        apiService.getMyEmergencies()
    }

    suspend fun createReport(request: CreateIncidentReportRequest): Result<IncidentReportResponse> = runCatchingApi {
        apiService.createReport(request)
    }

    suspend fun getMyReports(): Result<List<IncidentReportResponse>> = runCatchingApi {
        apiService.getMyReports()
    }

    suspend fun getMyHistory(): Result<List<HistoryItemResponse>> = runCatchingApi {
        apiService.getMyHistory()
    }

    suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlanResponse>> = runCatchingApi {
        apiService.getSubscriptionPlans()
    }

    suspend fun getMySubscription(): Result<UserSubscriptionResponse> = runCatchingApi {
        apiService.getMySubscription()
    }

    suspend fun updateSubscription(request: UpdateSubscriptionRequest): Result<UserSubscriptionResponse> = runCatchingApi {
        apiService.updateSubscription(request)
    }

    suspend fun getFamilyMembers(): Result<List<FamilyMemberResponse>> = runCatchingApi {
        apiService.getFamilyMembers()
    }

    suspend fun addFamilyMember(request: FamilyMemberRequest): Result<FamilyMemberResponse> = runCatchingApi {
        apiService.addFamilyMember(request)
    }

    suspend fun removeFamilyMember(id: Long): Result<String> = runCatchingApi {
        apiService.removeFamilyMember(id).message
    }

    suspend fun getFamilyMap(): Result<List<FamilyMapMemberResponse>> = runCatchingApi {
        apiService.getFamilyMap()
    }

    private suspend fun <T> runCatchingApi(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (httpException: HttpException) {
            val raw = httpException.response()?.errorBody()?.string().orEmpty()
            val message = parseApiError(raw) ?: "Ocurrio un error al conectar con la API"
            Result.failure(IllegalStateException(message))
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(exception.message ?: "Ocurrio un error inesperado"))
        }
    }

    private fun parseApiError(raw: String): String? {
        return try {
            Gson().fromJson(raw, ErrorResponse::class.java)?.message
        } catch (_: Exception) {
            null
        }
    }
}
