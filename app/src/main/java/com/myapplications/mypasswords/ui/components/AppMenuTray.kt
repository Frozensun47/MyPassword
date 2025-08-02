package com.myapplications.mypasswords.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.R
import com.myapplications.mypasswords.navigation.Screen

@Composable
fun AppMenuTray(
    navController: NavController,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.my_password_text),
                contentDescription = "App Logo",
                modifier = Modifier.height(40.dp)
            )
        }
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "About") },
            label = {Text("About")},
            selected = false,
            onClick = { navController.navigate(Screen.About.route) }
        )
        Spacer(Modifier.height(1.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = "Privacy") },
            label = {Text("Privacy")},
            selected = false,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/privacy"))
                context.startActivity(intent)
            }
        )
        Spacer(Modifier.height(1.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Gavel, contentDescription = "Terms and conditions") },
            label = {Text("Terms and conditions")},
            selected = false,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/terms"))
                context.startActivity(intent)
            }
        )
        Spacer(Modifier.height(1.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
            label = {Text("Security")},
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypasswords.myapplications.store/security"))
                context.startActivity(intent)
            },
            selected = false
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(5.dp))
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
        )
    }
}