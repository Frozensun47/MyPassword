// FILE: com/myapplications/mypasswords/ui/view/SettingsScreen.kt
package com.myapplications.mypasswords.ui.view

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel

/**
 * The main settings screen for the application.
 * Provides options for dangerous actions like deleting all data and links to other info pages.
 *
 * @param navController The NavController for handling navigation events.
 * @param mainViewModel The ViewModel for interacting with the repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Section for general app information.
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About MyPasswords",
                subtitle = "View version info and our security promise",
                onClick = { navController.navigate(Screen.About.route) }
            )
            HorizontalDivider()

            // A clearly marked, dangerous action.
            SettingsItem(
                icon = Icons.Default.Warning,
                title = "Delete All Data",
                subtitle = "Permanently erase all passwords and reset the app",
                onClick = { showDeleteDialog = true },
                titleColor = MaterialTheme.colorScheme.error
            )
            HorizontalDivider()
        }
    }

    // Confirmation dialog for the delete action.
    if (showDeleteDialog) {
        DeleteDataDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                mainViewModel.deleteAllData()
                // Restart the app to ensure a clean state.
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                val componentName = intent!!.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                context.startActivity(mainIntent)
                Runtime.getRuntime().exit(0) // Close the current process.
            }
        )
    }
}

/**
 * A reusable composable for displaying a single item in a settings list.
 *
 * @param icon The icon to display for the setting.
 * @param title The main title of the setting.
 * @param subtitle A brief description of the setting.
 * @param onClick The action to perform when the item is clicked.
 * @param titleColor The color of the title text, defaults to the current content color.
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = LocalContentColor.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = titleColor, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = LocalContentColor.current.copy(alpha = 0.6f))
        }
    }
}
