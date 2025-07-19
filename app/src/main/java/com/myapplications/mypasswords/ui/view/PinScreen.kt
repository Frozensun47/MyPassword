package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapplications.mypasswords.ui.viewmodel.PinViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun PinScreen(
    mode: PinViewModel.PinMode,
    onSuccess: () -> Unit,
    pinViewModel: PinViewModel = viewModel()
) {
    val context = LocalContext.current
    val pinState by pinViewModel.pinState.collectAsState()

    // Timer for lockout countdown
    var remainingTime by remember { mutableStateOf("") }
    LaunchedEffect(key1 = pinState.lockoutUntil) {
        while (pinState.lockoutUntil > System.currentTimeMillis()) {
            val remainingMillis = pinState.lockoutUntil - System.currentTimeMillis()
            remainingTime = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(remainingMillis),
                TimeUnit.MILLISECONDS.toSeconds(remainingMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingMillis))
            )
            delay(1000)
        }
        remainingTime = ""
        pinViewModel.clearLockoutIfExpired(context) // Check again when timer finishes
    }

    // Initial check
    LaunchedEffect(Unit) {
        pinViewModel.checkInitialLockout(context)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = mode.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = mode.subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pinState.pin,
            onValueChange = { pinViewModel.onPinChanged(it) },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = pinState.errorMessage != null,
            singleLine = true,
            enabled = !pinState.isLocked
        )

        pinState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }

        if (pinState.isLocked && remainingTime.isNotEmpty()) {
            Text("Try again in $remainingTime", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { pinViewModel.submitPin(context, mode, onSuccess) },
            enabled = !pinState.isLocked
        ) {
            Text("Continue")
        }
    }
}