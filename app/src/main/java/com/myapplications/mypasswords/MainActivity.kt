// FILE: com/myapplications/mypasswords/MainActivity.kt
package com.myapplications.mypasswords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.myapplications.mypasswords.navigation.AppNavigation
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.ui.AppLifecycleHandler
import com.myapplications.mypasswords.ui.theme.MyPasswordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the repository as soon as the app starts.
        PasswordRepository.initialize(applicationContext)

        setContent {
            MyPasswordsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppLifecycleHandler(navController = navController)

                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
