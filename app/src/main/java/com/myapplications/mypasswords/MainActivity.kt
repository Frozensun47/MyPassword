// FILE: com/myapplications/mypasswords/MainActivity.kt
package com.myapplications.mypasswords

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity created.")

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
                    Log.d(TAG, "setContent: Composable content is being set up. keepSplashOnScreen is initially true.")

                    LaunchedEffect(Unit) {
                        Log.d(TAG, "LaunchedEffect started on thread: ${Thread.currentThread().name}")
                        withContext(Dispatchers.IO) {
                            Log.d(TAG, "Switched to IO context for initial checks.")
                            try {
                                if (isDeviceRooted() || isRunningOnEmulator()) {
                                    securityAlert = "For your security, this app cannot be run on a rooted or emulated device."
                                    Log.w(TAG, "Security check failed: Rooted device or emulator detected.")
                                } else {
                                    Log.d(TAG, "Security check passed. Determining start destination.")
                                    // The repository initialization is already running in the background.
                                    // This call will simply suspend until it's done.
                                    val securityManager = SecurityManager()
                                    val isPinSet = securityManager.isPinSet(context)
                                    startDestination = if (isPinSet) Screen.PinAuth.route else Screen.Onboarding.route
                                    Log.d(TAG, "Start destination determined: $startDestination")
                                }
                            } catch (e: Exception) {
                                securityAlert = "Failed to start application: ${e.message}"
                                Log.e(TAG, "Error during initial setup", e)
                            } finally {
                                keepSplashOnScreen = false
                                Log.d(TAG, "Finished initial setup. keepSplashOnScreen set to false.")
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
                    } else {
                        // This loading indicator will now be shown while the LaunchedEffect runs.
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
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
        // ... (rest of the function is unchanged)
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
        // ... (rest of the function is unchanged)
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