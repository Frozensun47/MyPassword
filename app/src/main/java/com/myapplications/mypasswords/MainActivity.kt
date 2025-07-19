package com.myapplications.mypasswords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.myapplications.mypasswords.navigation.AppNavigation
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.security.SecurityManager
import com.myapplications.mypasswords.ui.theme.MyPasswordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val securityManager = SecurityManager(this)
        val startDestination = if (securityManager.isPinSet()) {
            Screen.PinAuth.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            MyPasswordsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}