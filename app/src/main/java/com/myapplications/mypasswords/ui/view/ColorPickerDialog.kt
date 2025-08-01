package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FFFFFF", "#F28B82", "#FCCB04", "#CCFF90",
        "#A7FFEB", "#CBF0F8", "#AECBFA", "#D7AEFB",
        "#FDCFE8", "#E6C9A8", "#E8EAED"
    ) // White, Red, Yellow, Green, Teal, Blue, etc.

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Card Color") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 16.dp)
            ) {
                items(colors) { colorHex ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(hexToColor(colorHex) ?: Color.Transparent)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable { onColorSelected(colorHex) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}