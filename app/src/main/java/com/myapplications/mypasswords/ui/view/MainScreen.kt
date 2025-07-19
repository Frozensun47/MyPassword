package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.R // Make sure R is imported
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.navigation.Screen
import com.myapplications.mypasswords.ui.components.AppMenuTray
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val passwords = mainViewModel.getPasswords(context).collectAsState(initial = emptyList())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppMenuTray(navController = navController) {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.PasswordDetail.createRoute("new")) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Password")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // This is the new, definitive header layout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 7.dp) // Minimal vertical padding
                ) {
                    // Centered Logo with a specific height
                    Image(
                        painter = painterResource(id = R.drawable.my_password_text),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            // Set a specific height. The width will adjust automatically.
                            .height(40.dp)
                            .align(Alignment.Center)
                    )

                    // Menu Icon aligned to the left
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }

                // The rest of your screen content
                if (passwords.value.isEmpty()) {
                    EmptyState(PaddingValues())
                } else {
                    PasswordList(
                        passwords = passwords.value,
                        navController = navController,
                        paddingValues = PaddingValues()
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
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 8.dp)
    ) {
        items(passwords) { password ->
            PasswordCard(password = password) {
                // This is the key change. The call is now clear and unambiguous.
                navController.navigate(Screen.PasswordPinVerify.createRoute(password.id))
            }
        }
    }
}
@Composable
fun PasswordCard(password: Password, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        // This is the key change to fix the card color
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    // Use onSurface color for better readability
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = password.username,
                    style = MaterialTheme.typography.bodyMedium,
                    // Use a slightly dimmed onSurface color
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyState(paddingValues: PaddingValues) {
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