package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    navController: NavController,
    passwordId: String?,
    mainViewModel: MainViewModel = viewModel()
) {
    val isNewPassword = passwordId == "new" || passwordId == null

    var password by remember { mutableStateOf<Password?>(null) }
    var isLoading by remember { mutableStateOf(!isNewPassword) }

    LaunchedEffect(passwordId) {
        if (!isNewPassword) {
            password = mainViewModel.getPasswordById(passwordId!!)
            isLoading = false
        }
    }

    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf<String?>(null) }
    var colorHex by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    val folders by mainViewModel.getPasswords().collectAsState(initial = emptyList())
        .let { state -> derivedStateOf { state.value.mapNotNull { it.folder }.distinct().sorted() } }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(password) {
        if (password != null && !isNewPassword) {
            title = password!!.title
            username = password!!.username
            passwordValue = password!!.password
            folder = password!!.folder
            colorHex = password!!.colorHex
        }
    }

    // Get the default color outside of the remember block
    val defaultColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedColor = remember(colorHex) {
        colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: defaultColor
    }
    val onSelectedColor = remember(selectedColor) {
        if (ColorUtils.calculateLuminance(selectedColor.toArgb()) > 0.5) Color.Black else Color.White
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = { color: Color ->
                val colorInt = color.toArgb()

                // Your existing logic is correct for converting the Int to a Hex String
                colorHex = String.format("#%06X", 0xFFFFFF and colorInt)

                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }


    if (showFolderDialog) {
        FolderEditDialog(
            onDismiss = { showFolderDialog = false },
            onConfirm = { newFolderName ->
                folder = newFolderName
                showFolderDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewPassword) "Add Password" else "Edit Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNewPassword && password != null) {
                        IconButton(onClick = {
                            mainViewModel.deletePassword(password!!)
                            navController.popBackStack(Screen.Main.route, inclusive = false)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (title.isNotBlank() && username.isNotBlank() && passwordValue.isNotBlank()) {
                FloatingActionButton(
                    onClick = {
                        val passwordToSave = if (isNewPassword) {
                            Password(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                username = username.trim(),
                                password = passwordValue,
                                folder = folder?.trim()?.takeIf { it.isNotBlank() },
                                colorHex = colorHex
                            )
                        } else {
                            password!!.copy(
                                title = title.trim(),
                                username = username.trim(),
                                password = passwordValue,
                                folder = folder?.trim()?.takeIf { it.isNotBlank() },
                                colorHex = colorHex
                            )
                        }
                        mainViewModel.savePassword(passwordToSave)
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Save")
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username or Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = passwordValue,
                    onValueChange = { passwordValue = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = folder ?: "No Folder",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Folder") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        // Replace the deprecated modifier here
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable) // Correct usage
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Folder") },
                            onClick = {
                                folder = null
                                expanded = false
                            }
                        )
                        folders.forEach { folderName ->
                            DropdownMenuItem(
                                text = { Text(folderName) },
                                onClick = {
                                    folder = folderName
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Create New Folder...") },
                            onClick = {
                                showFolderDialog = true
                                expanded = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(selectedColor)
                        .clickable { showColorPicker = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Card Color", color = onSelectedColor)
                    Icon(Icons.Default.Palette, contentDescription = "Select Color", tint = onSelectedColor)
                }
            }
        }
    }
}
