// FILE: com/myapplications/mypasswords/ui/AppLifecycleHandler.kt
package com.myapplications.mypasswords.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.security.AuthManager
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppLifecycleHandler(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                AuthManager.isAuthenticated.value = false
            } else if (event == Lifecycle.Event.ON_START) {
                scope.launch {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    val openRoutes = setOf(
                        Screen.Splash.route,
                        Screen.Onboarding.route,
                        Screen.PinSetup.route,
                        Screen.PinAuth.route
                    )

                    // Move blocking I/O to a background thread
                    val needsAuth = withContext(Dispatchers.IO) {
                        !AuthManager.isAuthenticated.value &&
                                SecurityManager().isPinSet(context) &&
                                currentRoute !in openRoutes
                    }

                    if (needsAuth) {
                        navController.navigate(Screen.PinAuth.route) {
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
