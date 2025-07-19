package com.myapplications.mypasswords.ui.view

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun DeleteDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationText by remember { mutableStateOf("") }
    val isConfirmEnabled = confirmationText.equals("delete", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure?") },
        text = {
            OutlinedTextField(
                value = confirmationText,
                onValueChange = { confirmationText = it },
                label = { Text("Type 'delete' to confirm") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}