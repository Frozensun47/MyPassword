// FILE: com/myapplications/mypasswords/ui/view/PinScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapplications.mypasswords.ui.viewmodel.PinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinScreen(
    mode: PinViewModel.PinMode,
    onSuccess: () -> Unit,
    pinViewModel: PinViewModel = viewModel()
) {
    // Corrected: Access the new uiState from the ViewModel
    val uiState by pinViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Corrected: Call the new initialize function
    LaunchedEffect(Unit) {
        pinViewModel.initialize(mode, onSuccess)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(40.dp) // Reserve space to prevent layout shifts
            )
            Spacer(modifier = Modifier.height(32.dp))

            PinIndicator(pinLength = uiState.enteredPin.length, error = uiState.showError)

            Spacer(modifier = Modifier.weight(1f))

            PinKeypad(
                onNumberClick = { number -> pinViewModel.onPinDigit(number) },
                onBackspaceClick = { pinViewModel.onBackspace() }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Handle success navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(100)
            onSuccess()
        }
    }

    // Handle error state by showing a message and then clearing it
    LaunchedEffect(uiState.showError) {
        if (uiState.showError) {
            coroutineScope.launch {
                delay(1000) // Show error for 1 second
                pinViewModel.clearError()
            }
        }
    }
}

@Composable
fun PinIndicator(pinLength: Int, error: Boolean) {
    val shake by animateFloatAsState(
        targetValue = if (error) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = "shake"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .offset(x = (shake * 10f * kotlin.math.sin(shake * 2 * Math.PI * 5f)).dp), // Shake animation
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        repeat(PinViewModel.PIN_LENGTH) { index ->
            val isFilled = index < pinLength
            val color by animateColorAsState(
                targetValue = if (error) MaterialTheme.colorScheme.error else if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200), label = "color"
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun PinKeypad(onNumberClick: (String) -> Unit, onBackspaceClick: () -> Unit) {
    val buttons = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "back")

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(280.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(buttons.size) { index ->
            val item = buttons[index]
            when {
                item.isNotBlank() && item != "back" -> KeypadButton(
                    text = item,
                    onClick = { onNumberClick(item) }
                )
                item == "back" -> KeypadButton(
                    icon = Icons.Default.Backspace,
                    onClick = onBackspaceClick
                )
                else -> Spacer(modifier = Modifier.size(72.dp)) // Empty space for layout
            }
        }
    }
}

@Composable
fun KeypadButton(
    onClick: () -> Unit,
    text: String? = null,
    icon: ImageVector? = null
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text != null) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
