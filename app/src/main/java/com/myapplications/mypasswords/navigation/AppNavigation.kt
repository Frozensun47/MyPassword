package com.myapplications.mypasswords.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myapplications.mypasswords.ui.view.*
import com.myapplications.mypasswords.ui.viewmodel.PinViewModel

/**
 * A cleaner, unambiguous definition for all navigation destinations.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object PinSetup : Screen("pin_setup")
    data object PinAuth : Screen("pin_auth")
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object About : Screen("about")

    // Use 'object' for routes with arguments and provide a clear helper function.
    data object PasswordDetail : Screen("password_detail/{passwordId}") {
        fun createRoute(passwordId: String?) = "password_detail/$passwordId"
    }

    data object PasswordPinVerify : Screen("password_pin_verify/{passwordId}") {
        fun createRoute(passwordId: String) = "password_pin_verify/$passwordId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(Screen.PinSetup.route) {
            PinScreen(mode = PinViewModel.PinMode.SETUP, onSuccess = {
                navController.navigate(Screen.Main.route) { popUpTo(Screen.PinSetup.route) { inclusive = true } }
            })
        }
        composable(Screen.PinAuth.route) {
            PinScreen(mode = PinViewModel.PinMode.AUTHENTICATE, onSuccess = {
                navController.navigate(Screen.Main.route) { popUpTo(Screen.PinAuth.route) { inclusive = true } }
            })
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.PasswordPinVerify.route) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            PinScreen(mode = PinViewModel.PinMode.VERIFY, onSuccess = {
                navController.navigate(Screen.PasswordDetail.createRoute(passwordId)) {
                    popUpTo(Screen.PasswordPinVerify.route) { inclusive = true }
                }
            })
        }
        composable(Screen.PasswordDetail.route) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            PasswordDetailScreen(navController = navController, passwordId = passwordId)
        }
    }
}