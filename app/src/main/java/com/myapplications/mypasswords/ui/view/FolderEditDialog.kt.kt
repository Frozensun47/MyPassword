package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.myapplications.mypasswords.model.Password

@Composable
fun FolderEditDialog(
    password: Password,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf(password.folder ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Folder") },
        text = {
            Column {
                Text("Enter a folder name for '${password.title}'.")
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(folderName) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}