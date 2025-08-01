package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.R
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.components.AppMenuTray
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// Helper function to convert a hex string to a Compose Color
fun hexToColor(hex: String?): Color? {
    if (hex == null) return null
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        null // Return null if the hex string is invalid
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val passwords = mainViewModel.getPasswords().collectAsState(initial = emptyList())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- State for long-press menu and dialogs ---
    var showMenu by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var selectedPassword by remember { mutableStateOf<Password?>(null) }

    // --- Show Folder Dialog ---
    if (showFolderDialog && selectedPassword != null) {
        FolderEditDialog(
            // Pass the password's current folder name to the correct parameter
            initialFolderName = selectedPassword!!.folder ?: "",
            onDismiss = { showFolderDialog = false },
            onConfirm = { folderName ->
                mainViewModel.updatePasswordFolder(selectedPassword!!.id, folderName)
                showFolderDialog = false
            }
        )
    }

    // --- Show Color Picker Dialog ---
    if (showColorDialog && selectedPassword != null) {
        ColorPickerDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = { color: Color ->
                val hexString = String.format("#%06X", 0xFFFFFF and color.toArgb())
                mainViewModel.updatePasswordColor(selectedPassword!!.id, hexString)
                showColorDialog = false
            }
        )
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppMenuTray(
                closeDrawer = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate(Screen.Settings.route)
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Centered Logo
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.my_password_text),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .height(35.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    },
                    navigationIcon = {
                        // Menu Icon
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // --- ADD BUTTON MOVED TO TOP APP BAR ---
                        IconButton(onClick = { navController.navigate(Screen.PasswordDetail.createRoute("new")) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Password")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            // --- FLOATING ACTION BUTTON REMOVED ---
        ) { paddingValues ->
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (passwords.value.isEmpty()) {
                        EmptyState(PaddingValues())
                    } else {
                        PasswordList(
                            passwords = passwords.value,
                            navController = navController,
                            paddingValues = PaddingValues(),
                            onLongPress = { password ->
                                selectedPassword = password
                                showMenu = true
                            }
                        )
                    }
                }

                // --- Long-press dropdown menu ---
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Folder") },
                        onClick = {
                            showMenu = false
                            showFolderDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Folder, "Folder") }
                    )
                    DropdownMenuItem(
                        text = { Text("Change Color") },
                        onClick = {
                            showMenu = false
                            showColorDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Palette, "Color") }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordList(
    passwords: List<Password>,
    navController: NavController,
    paddingValues: PaddingValues,
    onLongPress: (Password) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 8.dp)
    ) {
        items(passwords) { password ->
            PasswordCard(
                password = password,
                onClick = {
                    navController.navigate(Screen.PasswordPinVerify.createRoute(password.id))
                },
                onLongClick = { onLongPress(password) } // Pass the long click event up
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordCard(
    password: Password,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardColor = hexToColor(password.colorHex) ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable( // --- IMPLEMENTED combinedClickable ---
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = password.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Display folder if it exists
                if (!password.folder.isNullOrBlank()) {
                    Text(
                        text = "Folder: ${password.folder}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = password.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyState(paddingValues: PaddingValues) {
    // This composable remains the same
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Passwords Yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Tap the '+' button to add your first password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}