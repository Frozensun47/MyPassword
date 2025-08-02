// FILE: com/myapplications/mypasswords/ui/view/SharedComponents.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

import androidx.compose.foundation.Image

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myapplications.mypasswords.R
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(onMenuClick: () -> Unit, onCreateFolderClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.my_password_text),
                    contentDescription = "App Logo",
                    modifier = Modifier.height(35.dp).align(Alignment.Center)
                )
            }
        },
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
                Icon(Icons.Default.MoveToInbox, contentDescription = "Move to Folder")
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

@Composable
fun PasswordCard(
    password: Password,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SelectableItemCard(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
        icon = Icons.Default.Lock,
        title = password.title,
        subtitle = password.username
    )
}

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
        subtitle = null
    )
}

@Composable
fun SelectableItemCard(
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
            .shadow(elevation, CardDefaults.shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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
                if (subtitle != null) {
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


@Composable
fun MoveToFolderDialog(
    selectedItemCount: Int,
    viewModel: MainViewModel,
    currentFolderId: String?,
    onDismiss: () -> Unit,
    onConfirm: (folderId: String?) -> Unit
) {
    val folders by viewModel.getAllFolders().collectAsState(initial = emptyList())
    var showFolderEditDialog by remember { mutableStateOf(false) }

    // This dialog will appear on top of the current one when triggered
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    DialogRow(
                        icon = Icons.Default.Home,
                        text = "Home (Remove from folder)",
                        onClick = { onConfirm(null) }
                    )
                }
                items(folders.filter { it.id != currentFolderId }) { folder ->
                    DialogRow(
                        icon = Icons.Default.Folder,
                        text = folder.name,
                        onClick = { onConfirm(folder.id) }
                    )
                }
                item {
                    DialogRow(
                        icon = Icons.Default.CreateNewFolder,
                        text = "Create New Folder",
                        onClick = { showFolderEditDialog = true }
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

@Composable
private fun DialogRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


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
