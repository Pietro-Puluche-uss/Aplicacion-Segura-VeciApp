package com.pietropuluche.veciapp.ui.navigation

sealed class Route(val value: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object Emergency : Route("emergency")
    data object Report : Route("report")
    data object History : Route("history")
    data object Profile : Route("profile")
    data object Subscription : Route("subscription")
    data object Family : Route("family")
}
