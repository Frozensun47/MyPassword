package com.myapplications.mypasswords.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.mypasswords_logo_foreground), // Your app icon
                contentDescription = "App Icon",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("MyPasswords", style = MaterialTheme.typography.headlineSmall)
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))

            AboutItem(
                text = "Website",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myprescription.myapplications.store"))
                    context.startActivity(intent)
                }
            )
            Divider()
            AboutItem(
                text = "Terms & Conditions",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myprescription.myapplications.store/terms"))
                    context.startActivity(intent)
                }
            )
            Divider()
            AboutItem(
                text = "Privacy Policy",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://your-website.com/privacy"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun AboutItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(
            Icons.AutoMirrored.Filled.Launch,
            contentDescription = "Open Link",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}