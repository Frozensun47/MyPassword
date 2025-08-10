// FILE: com/myapplications/mypasswords/ui/view/PasswordDetailScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

// A temporary state holder for the UI
private data class CredentialState(
    val id: String = UUID.randomUUID().toString(),
    var username: String,
    var password: String,
    var passwordVisible: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PasswordDetailScreen(
    navController: NavController,
    passwordId: String?,
    folderId: String?,
    mainViewModel: MainViewModel = viewModel()
) {
    val isNewEntry = passwordId == "new" || passwordId == null
    var title by remember { mutableStateOf("") }
    var credentials by remember { mutableStateOf(listOf(CredentialState(username = "", password = ""))) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Selection Mode State for Deleting Credentials ---
    var selectedCredentialIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val inSelectionMode = selectedCredentialIds.isNotEmpty()

    LaunchedEffect(passwordId) {
        if (!isNewEntry) {
            mainViewModel.getEntryWithCredentials(passwordId!!)?.collect { entryWithCreds ->
                if (entryWithCreds != null) {
                    title = entryWithCreds.entry.title
                    credentials = entryWithCreds.credentials.map {
                        CredentialState(id = it.id, username = it.username, password = it.password)
                    }
                }
            }
        }
    }

    fun handleSave() {
        val entryToSave = PasswordEntry(
            id = if (isNewEntry) UUID.randomUUID().toString() else passwordId!!,
            title = title.trim(),
            folderId = folderId
        )
        val credentialsToSave = credentials.map {
            Credential(
                id = it.id,
                entryId = entryToSave.id,
                username = it.username.trim(),
                password = it.password
            )
        }
        mainViewModel.saveEntryWithCredentials(entryToSave, credentialsToSave)
        navController.popBackStack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedContent(
                targetState = inSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "DetailTopAppBarAnimation"
            ) { selectionActive ->
                if (selectionActive) {
                    // Contextual action bar for when an item is selected
                    TopAppBar(
                        title = { Text("${selectedCredentialIds.size} selected") },
                        navigationIcon = {
                            IconButton(onClick = { selectedCredentialIds = emptySet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                credentials = credentials.filterNot { it.id in selectedCredentialIds }
                                selectedCredentialIds = emptySet()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected account(s)")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                } else {
                    // Standard top app bar
                    TopAppBar(
                        title = { Text(if (isNewEntry) "Add Entry" else "Edit Entry") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            if (!isNewEntry) {
                                IconButton(onClick = {
                                    val entryToDelete = PasswordEntry(id = passwordId!!, title = title, folderId = folderId)
                                    mainViewModel.deleteEntry(entryToDelete)
                                    navController.popBackStack()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Entry")
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val isFormValid = title.isNotBlank() && credentials.all { it.username.isNotBlank() && it.password.isNotBlank() }
            // Hide FAB when in selection mode
            AnimatedVisibility(visible = isFormValid && !inSelectionMode) {
                FloatingActionButton(onClick = ::handleSave) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Google, Banking)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !inSelectionMode // Disable editing in selection mode
                )
            }

            items(credentials, key = { it.id }) { credState ->
                CredentialInputCard(
                    credentialState = credState,
                    isSelected = credState.id in selectedCredentialIds,
                    enabled = !inSelectionMode,
                    snackbarHostState = snackbarHostState,
                    onUpdate = { updatedState ->
                        credentials = credentials.map { if (it.id == updatedState.id) updatedState else it }
                    },
                    onClick = {
                        if (inSelectionMode) {
                            selectedCredentialIds = if (credState.id in selectedCredentialIds) {
                                selectedCredentialIds - credState.id
                            } else {
                                selectedCredentialIds + credState.id
                            }
                        }
                    },
                    onLongClick = {
                        if (!inSelectionMode && credentials.size > 1) {
                            selectedCredentialIds = setOf(credState.id)
                        }
                    }
                )
            }

            item {
                // Hide the "Add" button when in selection mode
                if (!inSelectionMode) {
                    OutlinedButton(
                        onClick = { credentials = credentials + CredentialState(username = "", password = "") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Add another account")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CredentialInputCard(
    credentialState: CredentialState,
    isSelected: Boolean,
    enabled: Boolean,
    snackbarHostState: SnackbarHostState,
    onUpdate: (CredentialState) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val cardColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "card color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            BorderlessTextField(
                value = credentialState.username,
                onValueChange = { onUpdate(credentialState.copy(username = it)) },
                label = "Username or Email",
                enabled = enabled,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(credentialState.username))
                    scope.launch { snackbarHostState.showSnackbar("Username copied") }
                }
            )
            BorderlessTextField(
                value = credentialState.password,
                onValueChange = { onUpdate(credentialState.copy(password = it)) },
                label = "Password",
                enabled = enabled,
                isPassword = true,
                passwordVisible = credentialState.passwordVisible,
                onPasswordVisibilityChange = { onUpdate(credentialState.copy(passwordVisible = !credentialState.passwordVisible)) },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(credentialState.password))
                    scope.launch { snackbarHostState.showSnackbar("Password copied") }
                }
            )
        }
    }
}

@Composable
private fun BorderlessTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    onCopy: () -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            Row {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = onPasswordVisibilityChange, enabled = enabled) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
                IconButton(onClick = onCopy, enabled = enabled) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
