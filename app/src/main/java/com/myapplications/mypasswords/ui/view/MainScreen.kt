// FILE: com/myapplications/mypasswords/ui/view/MainScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.HomeItem
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.components.AppMenuTray
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val homeItems by mainViewModel.homeItems.collectAsState(initial = emptyList())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showFolderDialog by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // --- Selection Mode State ---
    var inSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<HomeItem>>(emptySet()) }

    fun clearSelection() {
        inSelectionMode = false
        selectedItems = emptySet()
    }

    // --- Dialogs ---
    if (showFolderDialog) {
        FolderEditDialog(
            onDismiss = { showFolderDialog = false },
            onConfirm = { folderName ->
                mainViewModel.saveFolder(Folder(name = folderName))
                showFolderDialog = false
            }
        )
    }

    if (showMoveToFolderDialog) {
        MoveToFolderDialog(
            selectedItemCount = selectedItems.size,
            viewModel = mainViewModel,
            currentFolderId = null, // At root, so no current folder
            onDismiss = { showMoveToFolderDialog = false },
            onConfirm = { folderId ->
                mainViewModel.moveItemsToFolder(selectedItems, folderId)
                showMoveToFolderDialog = false
                clearSelection()
            }
        )
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            itemCount = selectedItems.size,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                mainViewModel.deleteItems(selectedItems)
                showDeleteConfirmation = false
                clearSelection()
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppMenuTray(closeDrawer = { scope.launch { drawerState.close() } }) }
    ) {
        Scaffold(
            topBar = {
                AnimatedContent(
                    targetState = inSelectionMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TopAppBar Animation"
                ) { isSelectionMode ->
                    if (isSelectionMode) {
                        SelectionTopAppBar(
                            selectedItemCount = selectedItems.size,
                            onClearSelection = { clearSelection() },
                            onMove = { showMoveToFolderDialog = true },
                            onDelete = { showDeleteConfirmation = true }
                        )
                    } else {
                        StandardTopAppBar(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onCreateFolderClick = { showFolderDialog = true }
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                if (!inSelectionMode) {
                    // **UI UPDATE**: Using LargeFloatingActionButton for a more prominent look
                    LargeFloatingActionButton(
                        onClick = { navController.navigate(Screen.PasswordDetail.createRoute("new")) }
                    ) {
                        Icon(
                            Icons.Default.PostAdd,
                            contentDescription = "Add Password",
                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp), // Added bottom padding for FAB
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(homeItems, key = { it.id }) { item ->
                    val isSelected = selectedItems.any { it.id == item.id }

                    fun handleItemClick() {
                        if (inSelectionMode) {
                            selectedItems = if (isSelected) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                            if (selectedItems.isEmpty()) {
                                inSelectionMode = false
                            }
                        } else {
                            when (item) {
                                is HomeItem.FolderItem -> navController.navigate(
                                    Screen.FolderDetail.createRoute(item.folder.id, item.folder.name)
                                )
                                is HomeItem.PasswordItem -> navController.navigate(
                                    Screen.PasswordPinVerify.createRoute(item.password.id)
                                )
                            }
                        }
                    }

                    fun handleItemLongClick() {
                        if (!inSelectionMode) {
                            inSelectionMode = true
                            selectedItems = setOf(item)
                        }
                    }

                    when (item) {
                        is HomeItem.FolderItem -> FolderCard(
                            folder = item.folder,
                            isSelected = isSelected,
                            onClick = ::handleItemClick,
                            onLongClick = ::handleItemLongClick
                        )
                        is HomeItem.PasswordItem -> PasswordCard(
                            password = item.password,
                            isSelected = isSelected,
                            onClick = ::handleItemClick,
                            onLongClick = ::handleItemLongClick
                        )
                    }
                }
            }
        }
    }
}
