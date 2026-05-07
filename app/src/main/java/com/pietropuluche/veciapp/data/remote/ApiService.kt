package com.pietropuluche.veciapp.data.remote

import com.pietropuluche.veciapp.data.model.ApiMessageResponse
import com.pietropuluche.veciapp.data.model.AuthResponse
import com.pietropuluche.veciapp.data.model.CreateEmergencyRequest
import com.pietropuluche.veciapp.data.model.CreateIncidentReportRequest
import com.pietropuluche.veciapp.data.model.DashboardHomeResponse
import com.pietropuluche.veciapp.data.model.EmergencyResponse
import com.pietropuluche.veciapp.data.model.FamilyMapMemberResponse
import com.pietropuluche.veciapp.data.model.FamilyInvitationResponse
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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("/")
    suspend fun rootHealth(): Map<String, String>

    @GET("api/health")
    suspend fun health(): Map<String, String>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/me/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("api/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @PUT("api/me/location")
    suspend fun updateLocation(@Body request: UpdateLocationRequest): ProfileResponse

    @GET("api/dashboard/home")
    suspend fun getDashboard(): DashboardHomeResponse

    @GET("api/report-categories")
    suspend fun getReportCategories(): List<ReportCategoryResponse>

    @POST("api/emergencies")
    suspend fun createEmergency(@Body request: CreateEmergencyRequest): EmergencyResponse

    @GET("api/emergencies/mine")
    suspend fun getMyEmergencies(): List<EmergencyResponse>

    @GET("api/emergencies/mine/{id}")
    suspend fun getMyEmergencyById(@Path("id") id: Long): EmergencyResponse

    @POST("api/reports")
    suspend fun createReport(@Body request: CreateIncidentReportRequest): IncidentReportResponse

    @GET("api/reports/mine")
    suspend fun getMyReports(): List<IncidentReportResponse>

    @GET("api/reports/mine/{id}")
    suspend fun getMyReportById(@Path("id") id: Long): IncidentReportResponse

    @GET("api/history/mine")
    suspend fun getMyHistory(): List<HistoryItemResponse>

    @GET("api/subscriptions/plans")
    suspend fun getSubscriptionPlans(): List<SubscriptionPlanResponse>

    @GET("api/subscriptions/me")
    suspend fun getMySubscription(): UserSubscriptionResponse

    @PUT("api/subscriptions/me")
    suspend fun updateSubscription(@Body request: UpdateSubscriptionRequest): UserSubscriptionResponse

    @GET("api/family/members")
    suspend fun getFamilyMembers(): List<FamilyMemberResponse>

    @GET("api/family/invitations/mine")
    suspend fun getMyFamilyInvitations(): List<FamilyInvitationResponse>

    @POST("api/family/members")
    suspend fun addFamilyMember(@Body request: FamilyMemberRequest): FamilyMemberResponse

    @POST("api/family/invitations/{id}/accept")
    suspend fun acceptFamilyInvitation(@Path("id") id: Long): ApiMessageResponse

    @POST("api/family/invitations/{id}/reject")
    suspend fun rejectFamilyInvitation(@Path("id") id: Long): ApiMessageResponse

    @DELETE("api/family/members/{id}")
    suspend fun removeFamilyMember(@Path("id") id: Long): ApiMessageResponse

    @DELETE("api/family/members/me")
    suspend fun leaveFamilyGroup(): ApiMessageResponse

    @GET("api/family/map")
    suspend fun getFamilyMap(): List<FamilyMapMemberResponse>
}
