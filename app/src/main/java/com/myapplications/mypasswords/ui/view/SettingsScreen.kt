package com.myapplications.mypasswords.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
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
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App information and links",
                onClick = { navController.navigate(Screen.About.route) }
            )
            Divider()
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy",
                subtitle = "Read our privacy policy",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/privacy"))
                    context.startActivity(intent)
                }
            )
            Divider()
            SettingsItem(
                icon = Icons.Default.Gavel,
                title = "Terms and conditions",
                subtitle = "Read our terms and conditions",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/terms"))
                    context.startActivity(intent)
                }
            )
            Divider()
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Security",
                subtitle = "Learn about our security measures",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/security"))
                    context.startActivity(intent)
                }
            )
            Divider()
            SettingsItem(
                icon = Icons.Default.Warning,
                title = "Delete All Data",
                subtitle = "Permanently erase all passwords and reset the app",
                onClick = { showDeleteDialog = true },
                titleColor = MaterialTheme.colorScheme.error
            )
            Divider()
        }
    }

    if (showDeleteDialog) {
        DeleteDataDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                mainViewModel.deleteAllData(context)
                // Restart the app
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                val componentName = intent!!.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                context.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        )
    }
}

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