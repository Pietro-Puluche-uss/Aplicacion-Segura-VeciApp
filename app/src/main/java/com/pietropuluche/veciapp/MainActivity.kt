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

                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        veciAppViewModel.bootstrap()
                    }
                }

                LaunchedEffect(currentRoute, authState.errorMessage, authState.infoMessage, appState.errorMessage, appState.successMessage) {
                    val message = authState.errorMessage.ifBlank {
                        authState.infoMessage.ifBlank {
                            appState.errorMessage.ifBlank { appState.successMessage }
                        }
                    }
                    if (message.isNotBlank()) {
                        if (
                            currentRoute == Route.Emergency.value &&
                            appState.successMessage == "Alerta enviada correctamente"
                        ) {
                            navController.navigate(Route.Home.value) {
                                popUpTo(Route.Home.value) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        snackbarHostState.showSnackbar(message)
                        authViewModel.clearMessage()
                        veciAppViewModel.clearMessages()
                    }
                }

                AppScaffold(
                    currentRoute = currentRoute,
                    snackbarHostState = snackbarHostState,
                    showBottomBar = currentRoute in listOf(
                        Route.Home.value,
                        Route.History.value,
                        Route.Profile.value,
                        Route.Family.value
                    ),
                    onNavigate = { route ->
                        if (route != currentRoute) {
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
                                onRefresh = { veciAppViewModel.bootstrap() },
                                onOpenEmergency = { navController.navigate(Route.Emergency.value) },
                                onOpenReport = { navController.navigate(Route.Report.value) },
                                onOpenSubscription = { navController.navigate(Route.Subscription.value) }
                            )
                        }
                        composable(Route.Emergency.value) {
                            EmergencyScreen(
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSubmit = { type, lat, lon, address, notes ->
                                    veciAppViewModel.createEmergency(type, lat, lon, address, notes)
                                }
                            )
                        }
                        composable(Route.Report.value) {
                            ReportScreen(
                                categories = appState.categories,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSubmit = { category, title, description, address, lat, lon ->
                                    veciAppViewModel.createReport(category, title, description, address, lat, lon)
                                }
                            )
                        }
                        composable(Route.History.value) {
                            HistoryScreen(history = appState.history)
                        }
                        composable(Route.Profile.value) {
                            ProfileScreen(
                                profile = appState.profile,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onSaveProfile = { firstName, lastName, phone, document, photoUrl ->
                                    veciAppViewModel.updateProfile(firstName, lastName, phone, document, photoUrl)
                                },
                                onUpdateLocation = { lat, lon, district, city ->
                                    veciAppViewModel.updateLocation(lat, lon, district, city)
                                },
                                onOpenSubscription = { navController.navigate(Route.Subscription.value) },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate(Route.Login.value) {
                                        popUpTo(Route.Home.value) { inclusive = true }
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
                                onSelectPlan = { veciAppViewModel.updateSubscription(it) }
                            )
                        }
                        composable(Route.Family.value) {
                            FamilyScreen(
                                familyMembers = appState.familyMembers,
                                familyMap = appState.familyMap,
                                successMessage = appState.successMessage,
                                errorMessage = appState.errorMessage,
                                onAddMember = { email, alias, relationship ->
                                    veciAppViewModel.addFamilyMember(email, alias, relationship)
                                },
                                onRemoveMember = { id ->
                                    veciAppViewModel.removeFamilyMember(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
