package com.pietropuluche.veciapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pietropuluche.veciapp.data.local.SessionManager
import com.pietropuluche.veciapp.data.remote.RetrofitClient
import com.pietropuluche.veciapp.data.repository.VeciAppRepository
import com.pietropuluche.veciapp.ui.common.AppScaffold
import com.pietropuluche.veciapp.ui.navigation.Route
import com.pietropuluche.veciapp.ui.screens.EmergencyScreen
import com.pietropuluche.veciapp.ui.screens.FamilyScreen
import com.pietropuluche.veciapp.ui.screens.HistoryScreen
import com.pietropuluche.veciapp.ui.screens.HomeScreen
import com.pietropuluche.veciapp.ui.screens.LoginScreen
import com.pietropuluche.veciapp.ui.screens.ProfileScreen
import com.pietropuluche.veciapp.ui.screens.RegisterScreen
import com.pietropuluche.veciapp.ui.screens.ReportScreen
import com.pietropuluche.veciapp.ui.screens.SplashScreen
import com.pietropuluche.veciapp.ui.screens.SubscriptionScreen
import com.pietropuluche.veciapp.ui.theme.VeciAppTheme
import com.pietropuluche.veciapp.ui.viewmodel.AuthViewModel
import com.pietropuluche.veciapp.ui.viewmodel.VeciAppViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            VeciAppTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val sessionManager = remember { SessionManager(applicationContext) }
                val repository = remember { VeciAppRepository(RetrofitClient.apiService) }
                val authViewModel = remember { AuthViewModel(repository, sessionManager) }
                val veciAppViewModel = remember { VeciAppViewModel(repository) }
                val authState by authViewModel.uiState.collectAsState()
                val appState by veciAppViewModel.uiState.collectAsState()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                LaunchedEffect(Unit) {
                    repository.warmUp()
                }

                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        veciAppViewModel.bootstrap()
                    }
                }

                LaunchedEffect(authState.isLoggedIn, currentRoute) {
                    val authenticatedRoute = currentRoute !in listOf(
                        null,
                        Route.Splash.value,
                        Route.Login.value,
                        Route.Register.value
                    )
                    if (authState.isLoggedIn && authenticatedRoute) {
                        while (true) {
                            delay(20000)
                            veciAppViewModel.refreshFamily()
                        }
                    }
                }

                LaunchedEffect(currentRoute, authState.errorMessage, authState.infoMessage, appState.errorMessage, appState.successMessage, appState.familyAlertMessage) {
                    val shouldHoldEmergencyConfirmation =
                        currentRoute == Route.Emergency.value &&
                            appState.pendingEmergencyConfirmation != null
                    val message = authState.errorMessage.ifBlank {
                        authState.infoMessage.ifBlank {
                            appState.familyAlertMessage.ifBlank {
                                if (shouldHoldEmergencyConfirmation) {
                                    appState.errorMessage
                                } else {
                                    appState.errorMessage.ifBlank { appState.successMessage }
                                }
                            }
                        }
                    }
                    if (message.isNotBlank()) {
                        if (currentRoute == Route.Report.value &&
                            appState.successMessage == "Reporte enviado correctamente"
                        ) {
                            navController.navigate(Route.Home.value) {
                                popUpTo(Route.Home.value) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        snackbarHostState.showSnackbar(message)
                        authViewModel.clearMessage()
                        veciAppViewModel.clearMessages()
                        veciAppViewModel.clearFamilyAlertMessage()
                    }
                }

                AppScaffold(
                    currentRoute = currentRoute,
                    snackbarHostState = snackbarHostState,
                    showBottomBar = currentRoute in listOf(
                        Route.Home.value,
                        Route.Emergency.value,
                        Route.History.value,
                        Route.Profile.value,
                        Route.Family.value,
                        Route.Subscription.value
                    ),
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            if (currentRoute == Route.Emergency.value) {
                                veciAppViewModel.clearEmergencyConfirmation()
                            }
                            if (currentRoute == Route.History.value) {
                                veciAppViewModel.closeHistoryDetail()
                            }
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                ) { modifier: Modifier ->
                    NavHost(
                        navController = navController,
                        startDestination = Route.Splash.value,
                        modifier = modifier
                    ) {
                        composable(Route.Splash.value) {
                            SplashScreen(authState.isLoggedIn) { logged ->
                                navController.navigate(if (logged) Route.Home.value else Route.Login.value) {
                                    popUpTo(Route.Splash.value) { inclusive = true }
                                }
                            }
                        }
                        composable(Route.Login.value) {
                            LoginScreen(
                                uiState = authState,
                                onLogin = { email, password ->
                                    authViewModel.login(email, password)
                                },
                                onGoRegister = {
                                    navController.navigate(Route.Register.value)
                                }
                            )
                            if (authState.isLoggedIn) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Route.Home.value) {
                                        popUpTo(Route.Login.value) { inclusive = true }
                                    }
                                }
                            }
                        }
                        composable(Route.Register.value) {
                            RegisterScreen(
                                uiState = authState,
                                onRegister = { firstName, lastName, email, password, phone, document ->
                                    authViewModel.register(firstName, lastName, email, password, phone, document)
                                },
                                onGoLogin = {
                                    navController.navigate(Route.Login.value) {
                                        popUpTo(Route.Register.value) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                            if (authState.isLoggedIn) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Route.Home.value) {
                                        popUpTo(Route.Login.value) { inclusive = true }
                                    }
                                }
                            }
                        }
                        composable(Route.Home.value) {
                            HomeScreen(
                                uiState = appState,
                                onOpenEmergency = {
                                    veciAppViewModel.clearEmergencyConfirmation()
                                    navController.navigate(Route.Emergency.value)
                                },
                                onOpenReport = { navController.navigate(Route.Report.value) },
                                onOpenProfile = { navController.navigate(Route.Profile.value) }
                            )
                        }
                        composable(Route.Emergency.value) {
                            EmergencyScreen(
                                errorMessage = appState.errorMessage,
                                emergencyConfirmation = appState.pendingEmergencyConfirmation,
                                onSubmit = { type, lat, lon, address, notes, evidenceImageBase64 ->
                                    veciAppViewModel.createEmergency(type, lat, lon, address, notes, evidenceImageBase64)
                                },
                                onGoHome = {
                                    veciAppViewModel.clearEmergencyConfirmation()
                                    navController.navigate(Route.Home.value) {
                                        popUpTo(Route.Home.value) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onClose = {
                                    veciAppViewModel.clearEmergencyConfirmation()
                                    if (!navController.popBackStack()) {
                                        navController.navigate(Route.Home.value) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                        composable(Route.Report.value) {
                            ReportScreen(
                                categories = appState.categories,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSubmit = { category, title, description, address, lat, lon, evidenceImageBase64 ->
                                    veciAppViewModel.createReport(category, title, description, address, lat, lon, evidenceImageBase64)
                                },
                                onClose = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate(Route.Home.value) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                        composable(Route.History.value) {
                            HistoryScreen(
                                history = appState.history,
                                selectedDetail = appState.selectedHistoryDetail,
                                isDetailLoading = appState.isHistoryDetailLoading,
                                onSelectItem = { veciAppViewModel.loadHistoryDetail(it) },
                                onClose = {
                                    veciAppViewModel.closeHistoryDetail()
                                    navController.navigate(Route.Home.value) {
                                        launchSingleTop = true
                                    }
                                },
                                onCloseDetail = { veciAppViewModel.closeHistoryDetail() }
                            )
                        }
                        composable(Route.Profile.value) {
                            ProfileScreen(
                                profile = appState.profile,
                                familyPreview = appState.familyMap,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSaveProfile = { email, phone ->
                                    veciAppViewModel.updateProfile(email, phone)
                                },
                                onUpdateLocation = { lat, lon, district, city ->
                                    veciAppViewModel.updateLocation(lat, lon, district, city)
                                },
                                onOpenSubscription = { navController.navigate(Route.Subscription.value) },
                                onOpenFamily = { navController.navigate(Route.Family.value) },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate(Route.Login.value) {
                                        popUpTo(Route.Home.value) { inclusive = true }
                                    }
                                },
                                onClose = {
                                    navController.navigate(Route.Home.value) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(Route.Subscription.value) {
                            SubscriptionScreen(
                                currentSubscription = appState.subscription,
                                plans = appState.plans,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSelectPlan = { veciAppViewModel.updateSubscription(it) },
                                onClose = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate(Route.Profile.value) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                        composable(Route.Family.value) {
                            FamilyScreen(
                                currentUserId = appState.profile?.userId,
                                currentPlan = appState.subscription?.currentPlan ?: appState.profile?.subscriptionPlan,
                                familyMembers = appState.familyMembers,
                                familyMap = appState.familyMap,
                                invitations = appState.familyInvitations,
                                emergencyAlerts = appState.familyEmergencyAlerts,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onRefresh = { veciAppViewModel.refreshFamily() },
                                onAddMember = { email, alias, relationship, groupType ->
                                    veciAppViewModel.addFamilyMember(email, alias, relationship, groupType)
                                },
                                onRemoveMember = { id ->
                                    veciAppViewModel.removeFamilyMember(id)
                                },
                                onAcceptInvitation = { id ->
                                    veciAppViewModel.acceptFamilyInvitation(id)
                                },
                                onRejectInvitation = { id ->
                                    veciAppViewModel.rejectFamilyInvitation(id)
                                },
                                onLeaveGroup = {
                                    veciAppViewModel.leaveFamilyGroup()
                                },
                                onDeleteAlert = { id ->
                                    veciAppViewModel.deleteFamilyAlert(id)
                                },
                                onClearAlerts = {
                                    veciAppViewModel.clearFamilyAlerts()
                                },
                                onOpenSubscription = { navController.navigate(Route.Subscription.value) },
                                onClose = {
                                    navController.navigate(Route.Home.value) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
