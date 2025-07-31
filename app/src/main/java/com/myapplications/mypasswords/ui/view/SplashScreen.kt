package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.R
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    // This effect runs when the screen is first displayed
    LaunchedEffect(Unit) {
        // Wait for 2 seconds
        delay(2000)

        // a new SecurityManager instance is created
        val securityManager = SecurityManager(context)

        // Determine the next screen based on whether a PIN is set
        val nextScreen = if (securityManager.isPinSet()) {
            Screen.PinAuth.route
        } else {
            Screen.Onboarding.route
        }

        // Navigate to the next screen and remove the splash screen from the back stack
        navController.navigate(nextScreen) {
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    // This is the UI for the splash screen
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.mypasswords_logo_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.my_password_text),
            contentDescription = "App Name"
        )
    }
}