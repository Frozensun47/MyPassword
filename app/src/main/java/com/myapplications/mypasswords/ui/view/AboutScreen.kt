package com.myapplications.mypasswords.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                title = { Text("About MyPasswords") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        // Use LazyColumn for better performance and a scrollable layout
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = R.mipmap.mypasswords_logo_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MyPasswords",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your digital security, uncompromised. MyPasswords is a 100% offline password manager that secures your data with military-grade AES-256 encryption.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SectionHeader(title = "Our Security Promise")
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Security Features List
            item {
                InfoCard(
                    icon = Icons.Default.CloudOff,
                    title = "Zero Internet Access",
                    description = "This app has no internet permission. It's physically impossible for your data to leave your device."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Security,
                    title = "Military-Grade Encryption",
                    description = "All data is encrypted with AES-256, the same standard trusted by governments and banks worldwide."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Face,
                    title = "Full Data Control",
                    description = "You have absolute control. No third parties can ever access your information."
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Website Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mypasswords.myapplications.store"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Our Website")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(title = "Support")
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "For any queries or feedback, please contact us:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "support@myapplications.store",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Divider(
            modifier = Modifier
                .width(60.dp)
                .padding(top = 4.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun InfoCard(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}