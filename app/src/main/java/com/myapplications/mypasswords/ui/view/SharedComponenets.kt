// FILE: com/myapplications/mypasswords/ui/view/SharedComponents.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.PasswordEntryWithCredentials
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel

/**
 * The standard top app bar for the main screen of the application.
 *
 * @param onMenuClick Callback for when the navigation menu icon is clicked.
 * @param onCreateFolderClick Callback for when the "Create Folder" action is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(onMenuClick: () -> Unit, onCreateFolderClick: () -> Unit) {
    TopAppBar(
        title = { Text("MyPasswords", fontWeight = FontWeight.Medium) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onCreateFolderClick) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder")
            }
        }
    )
}

/**
 * A contextual top app bar that appears when items are selected in a list.
 *
 * @param selectedItemCount The number of items currently selected.
 * @param onClearSelection Callback to clear the current selection.
 * @param onMove Callback to trigger the "move" action for the selected items.
 * @param onDelete Callback to trigger the "delete" action for the selected items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedItemCount: Int,
    onClearSelection: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedItemCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onMove) {
                Icon(Icons.Default.DriveFileMove, contentDescription = "Move to Folder")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Selected Items")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * A card composable for displaying a password entry in a list.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordCard(
    entryWithCredentials: PasswordEntryWithCredentials,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SelectableItemCard(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
        icon = Icons.Default.Lock,
        title = entryWithCredentials.entry.title,
        subtitle = entryWithCredentials.credentials.firstOrNull()?.username ?: ""
    )
}

/**
 * A card composable for displaying a folder in a list.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SelectableItemCard(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
        icon = Icons.Default.Folder,
        title = folder.name,
        subtitle = null // Folders don't have a subtitle
    )
}

/**
 * A generic, selectable card component used for both folders and password entries.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableItemCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    icon: ImageVector,
    title: String,
    subtitle: String?
) {
    val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp, label = "elevation")
    val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    scaleIn(animationSpec = spring()) togetherWith scaleOut(animationSpec = spring())
                },
                label = "Icon Animation"
            ) { selected ->
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A dialog that allows the user to move selected items to a different folder.
 */
@Composable
fun MoveToFolderDialog(
    viewModel: MainViewModel,
    currentFolderId: String?,
    onDismiss: () -> Unit,
    onConfirm: (folderId: String?) -> Unit
) {
    val folders by viewModel.getAllFolders().collectAsState(initial = emptyList())
    var showFolderEditDialog by remember { mutableStateOf(false) }

    if (showFolderEditDialog) {
        FolderEditDialog(
            onDismiss = { showFolderEditDialog = false },
            onConfirm = { folderName ->
                viewModel.saveFolder(Folder(name = folderName))
                showFolderEditDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to") },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("Home") },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        modifier = Modifier.clickable { onConfirm(null) }
                    )
                }
                items(folders.filter { it.id != currentFolderId }) { folder ->
                    ListItem(
                        headlineContent = { Text(folder.name) },
                        leadingContent = { Icon(Icons.Default.Folder, contentDescription = folder.name) },
                        modifier = Modifier.clickable { onConfirm(folder.id) }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                item {
                    ListItem(
                        headlineContent = { Text("Create New Folder") },
                        leadingContent = { Icon(Icons.Default.CreateNewFolder, contentDescription = "Create New Folder") },
                        modifier = Modifier.clickable { showFolderEditDialog = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * A dialog to confirm the deletion of one or more items.
 */
@Composable
fun DeleteConfirmationDialog(
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Items?") },
        text = { Text("Are you sure you want to permanently delete $itemCount selected item(s)? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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

/**
 * A dialog for creating a new folder or renaming an existing one.
 */
@Composable
fun FolderEditDialog(
    initialFolderName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf(initialFolderName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialFolderName.isEmpty()) "Create Folder" else "Rename Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim())
                    }
                },
                enabled = folderName.isNotBlank()
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
