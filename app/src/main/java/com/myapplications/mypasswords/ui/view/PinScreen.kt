package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.ui.viewmodel.PinViewModel

@Composable
fun PinScreen(navController: NavController, pinViewModel: PinViewModel = viewModel()) {
val context = LocalContext.current
var pin by remember { mutableStateOf("") }
val isPinSet = pinViewModel.isPinSet(context)

Column(
        modifier = Modifier
        .fillMaxSize()
            .padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
    ) {
Text(if (isPinSet) "Enter your PIN" else "Set your PIN", style = MaterialTheme.typography.headlineMedium)
Spacer(modifier = Modifier.height(16.dp))
TextField(
        value = pin,
        onValueChange = { pin = it },
label = { Text("PIN") }
        )
Spacer(modifier = Modifier.height(16.dp))
Button(onClick = {
    if (isPinSet) {
        if (pinViewModel.verifyPin(context, pin)) {
            navController.navigate("main")
        } else {
            // Show error
        }
    } else {
        pinViewModel.savePin(context, pin)
        navController.navigate("main")
    }
}) {
Text("Continue")
        }
                }
                }