// FILE: com/myapplications/mypasswords/ui/view/FolderDetailScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    navController: NavController,
    folderId: String,
    folderName: String,
    mainViewModel: MainViewModel = viewModel()
) {
    val passwordsInFolder by mainViewModel.getPasswordsInFolder(folderId).collectAsState(initial = emptyList())

    // --- Selection Mode State ---
    var inSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    fun clearSelection() {
        inSelectionMode = false
        selectedIds = emptySet()
    }

    // --- Dialogs ---
    if (showMoveToFolderDialog) {
        MoveToFolderDialog(
            selectedItemCount = selectedIds.size,
            viewModel = mainViewModel,
            currentFolderId = folderId,
            onDismiss = { showMoveToFolderDialog = false },
            onConfirm = { newFolderId ->
                mainViewModel.movePasswordsToFolderByIds(selectedIds, newFolderId)
                showMoveToFolderDialog = false
                clearSelection()
            }
        )
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            itemCount = selectedIds.size,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                mainViewModel.deletePasswordsByIds(selectedIds)
                showDeleteConfirmation = false
                clearSelection()
            }
        )
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = inSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "FolderDetailTopAppBar"
            ) { isSelectionMode ->
                if (isSelectionMode) {
                    SelectionTopAppBar(
                        selectedItemCount = selectedIds.size,
                        onClearSelection = { clearSelection() },
                        onMove = { showMoveToFolderDialog = true },
                        onDelete = { showDeleteConfirmation = true }
                    )
                } else {
                    TopAppBar(
                        title = { Text(folderName) },
                        navigationIcon = {
                            IconButton(onClick = {
                                // **THE FIX IS HERE**:
                                // This check prevents multiple back presses from firing before
                                // navigation can complete, which stops the app from crashing.
                                if (navController.currentBackStackEntry?.destination?.route == Screen.FolderDetail.route) {
                                    navController.popBackStack()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                mainViewModel.deleteFolder(Folder(id = folderId, name = folderName))
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Folder")
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (!inSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.PasswordDetail.createRoute("new", folderId))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Password")
                }
            }
        }
    ) { paddingValues ->
        if (passwordsInFolder.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("This folder is empty.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passwordsInFolder, key = { it.id }) { password ->
                    val isSelected = selectedIds.contains(password.id)
                    PasswordCard(
                        password = password,
                        isSelected = isSelected,
                        onClick = {
                            if (inSelectionMode) {
                                selectedIds = if (isSelected) {
                                    selectedIds - password.id
                                } else {
                                    selectedIds + password.id
                                }
                                if (selectedIds.isEmpty()) {
                                    inSelectionMode = false
                                }
                            } else {
                                navController.navigate(Screen.PasswordPinVerify.createRoute(password.id))
                            }
                        },
                        onLongClick = {
                            if (!inSelectionMode) {
                                inSelectionMode = true
                                selectedIds = setOf(password.id)
                            }
                        }
                    )
                }
            }
        }
    }
}
