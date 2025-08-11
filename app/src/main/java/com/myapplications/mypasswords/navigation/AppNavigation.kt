// FILE: com/myapplications/mypasswords/navigation/AppNavigation.kt
package com.myapplications.mypasswords.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myapplications.mypasswords.ui.view.*
import com.myapplications.mypasswords.ui.viewmodel.PinViewModel

/**
 * Defines all possible navigation destinations in the app with strongly-typed routes.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object PinSetup : Screen("pin_setup")
    data object PinAuth : Screen("pin_auth")
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object About : Screen("about")

    // Route for viewing the contents of a single folder.
    data object FolderDetail : Screen("folder_detail/{folderId}/{folderName}") {
        fun createRoute(folderId: String, folderName: String) = "folder_detail/$folderId/$folderName"
    }

    // Route for verifying PIN before showing a password's details.
    data object PasswordPinVerify : Screen("password_pin_verify/{passwordId}") {
        fun createRoute(passwordId: String) = "password_pin_verify/$passwordId"
    }

    // Route for creating or editing a password.
    // folderId is optional and used when creating a new password inside a specific folder.
    data object PasswordDetail : Screen("password_detail?passwordId={passwordId}&folderId={folderId}") {
        fun createRoute(passwordId: String, folderId: String? = null): String {
            val route = "password_detail?passwordId=$passwordId"
            return if (folderId != null) "$route&folderId=$folderId" else route
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.About.route) { AboutScreen(navController) }
        composable(Screen.Main.route) { MainScreen(navController) }

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
        composable(
            route = Screen.PasswordPinVerify.route,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            // The logic below is not ideal as it performs navigation within a composable.
            // It is kept here for now but should be handled by the calling view model
            // or a more robust navigation strategy in a production app.
            LaunchedEffect(key1 = passwordId) {
                navController.navigate(Screen.PasswordDetail.createRoute(passwordId)) {
                    popUpTo(Screen.PasswordPinVerify.route) {
                        inclusive = true
                    }
                }
            }
        }

        composable(
            route = Screen.PasswordDetail.route,
            arguments = listOf(
                navArgument("passwordId") { type = NavType.StringType },
                navArgument("folderId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            PasswordDetailScreen(
                navController = navController,
                passwordId = backStackEntry.arguments?.getString("passwordId"),
                folderId = backStackEntry.arguments?.getString("folderId")
            )
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""

            FolderDetailScreen(
                navController = navController,
                folderId = folderId,
                folderName = folderName
            )
        }
    }
}
