package com.myapplications.mypasswords.ui.view;

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.navigation.Screen

@Composable
fun OnboardingScreen(navController: NavController) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to MyPasswords!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Here are some important things to know:")
        Spacer(modifier = Modifier.height(16.dp))
        Text("- Your data is stored securely and encrypted on your device.")
        Text("- There is no cloud backup. If you lose your device, you lose your data.")
        Text("- Your PIN cannot be changed once set.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigate(Screen.Pin.route) }) {
            Text("I Understand, Let's Get Started")
        }
    }
}