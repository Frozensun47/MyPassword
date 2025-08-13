// FILE: com/myapplications/mypasswords/ui/view/DeleteDataDialog.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeleteDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationText by remember { mutableStateOf("") }
    val isConfirmEnabled = confirmationText.equals("delete", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you absolutely sure?") },
        text = {
            Column {
                Text("This action cannot be undone. This will permanently delete all of your data.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    label = { Text("Type 'delete' to confirm") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Everything")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}