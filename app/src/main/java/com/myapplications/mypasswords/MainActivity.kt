// FILE: com/myapplications/mypasswords/MainActivity.kt
package com.myapplications.mypasswords

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.myapplications.mypasswords.navigation.AppNavigation
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.ui.AppLifecycleHandler
import com.myapplications.mypasswords.ui.theme.MyPasswordsTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var securityAlert by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set secure flag to prevent screenshots and screen recording.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // --- NEW: Security Checks ---
        if (isDeviceRooted() || isRunningOnEmulator()) {
            securityAlert = "For your security, this app cannot be run on a rooted or emulated device."
        } else {
            // Only initialize the repository and set content if the device is secure.
            PasswordRepository.initialize(applicationContext)
        }

        setContent {
            MyPasswordsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (securityAlert != null) {
                        // If a security alert is present, show the dialog and do not load the app.
                        SecurityAlertDialog(
                            message = securityAlert!!,
                            onDismiss = { finish() } // Exit the app when dismissed
                        )
                    } else {
                        // If device is secure, load the full app.
                        val navController = rememberNavController()
                        AppLifecycleHandler(navController = navController)
                        AppNavigation(navController = navController)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // --- NEW: Clear clipboard when the app goes into the background ---
        clearClipboard()
    }

    private fun clearClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // Set the clipboard to an empty clip to clear it.
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
