package com.myapplications.mypasswords.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myapplications.mypasswords.ui.view.MainScreen
import com.myapplications.mypasswords.ui.view.OnboardingScreen
import com.myapplications.mypasswords.ui.view.PasswordDetailScreen
import com.myapplications.mypasswords.ui.view.PinScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Pin : Screen("pin")
    object Main : Screen("main")
    object PasswordDetail : Screen("password_detail/{passwordId}") {
        fun createRoute(passwordId: String?) = "password_detail/$passwordId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(Screen.Pin.route) {
            PinScreen(navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController)
        }
        composable(Screen.PasswordDetail.route) { backStackEntry ->
                val passwordId = backStackEntry.arguments?.getString("passwordId")
            PasswordDetailScreen(navController, passwordId)
        }
    }
}