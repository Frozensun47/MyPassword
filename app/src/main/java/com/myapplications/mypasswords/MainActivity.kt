// FILE: com/myapplications/mypasswords/MainActivity.kt
package com.myapplications.mypasswords

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.myapplications.mypasswords.navigation.AppNavigation
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.security.SecurityManager
import com.myapplications.mypasswords.ui.AppLifecycleHandler
import com.myapplications.mypasswords.ui.theme.MyPasswordsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            val context = LocalContext.current
            MyPasswordsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var securityAlert by remember { mutableStateOf<String?>(null) }
                    var startDestination by remember { mutableStateOf<String?>(null) }
                    var keepSplashOnScreen by remember { mutableStateOf(true) }

                    splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            try {
                                if (isDeviceRooted() || isRunningOnEmulator()) {
                                    securityAlert = "For your security, this app cannot be run on a rooted or emulated device."
                                } else {
                                    val securityManager = SecurityManager()
                                    val isPinSet = securityManager.isPinSet(context)
                                    startDestination = if (isPinSet) Screen.PinAuth.route else Screen.Onboarding.route
                                }
                            } catch (e: Exception) {
                                securityAlert = "Failed to start application: ${e.message}"
                            } finally {
                                keepSplashOnScreen = false
                            }
                        }
                    }

                    if (securityAlert != null) {
                        SecurityAlertDialog(
                            message = securityAlert!!,
                            onDismiss = { finish() }
                        )
                    } else if (startDestination != null) {
                        val navController = rememberNavController()
                        AppLifecycleHandler(navController = navController)
                        AppNavigation(navController = navController, startDestination = startDestination!!)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        clearClipboard()
    }

    private fun clearClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", "")
        clipboard.setPrimaryClip(clip)
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
}

@Composable
fun SecurityAlertDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Security Alert") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Exit App")
            }
        }
    )
}
