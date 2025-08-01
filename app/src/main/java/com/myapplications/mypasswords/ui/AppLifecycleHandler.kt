package com.myapplications.mypasswords.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.security.AuthManager
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.launch

@Composable
fun AppLifecycleHandler(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // Use a coroutine scope tied to the Composable

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // When the app goes to the background, mark as not authenticated.
                AuthManager.isAuthenticated.value = false
            } else if (event == Lifecycle.Event.ON_START) {
                // When the app comes to the foreground, check if authentication is needed.
                // --- LAUNCH A COROUTINE FOR THE SUSPEND FUNCTION CALL ---
                scope.launch {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route

                    // List of screens that do not require a PIN check on resume
                    val openRoutes = setOf(
                        Screen.Splash.route,
                        Screen.Onboarding.route,
                        Screen.PinSetup.route,
                        Screen.PinAuth.route
                    )

                    // The call to isPinSet() is now safely inside a coroutine
                    val needsAuth = !AuthManager.isAuthenticated.value &&
                            SecurityManager(context).isPinSet() &&
                            currentRoute !in openRoutes

                    if (needsAuth) {
                        navController.navigate(Screen.PinAuth.route) {
                            // Clear the entire back stack and make PinAuth the new root.
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}