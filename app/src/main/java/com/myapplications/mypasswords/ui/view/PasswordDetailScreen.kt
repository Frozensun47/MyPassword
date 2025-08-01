package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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
    passwordId: String,
    mainViewModel: MainViewModel = viewModel()
) {
    val isNewPassword = passwordId == "new"
    val password = if (isNewPassword) {
        Password(id = UUID.randomUUID().toString(), title = "", username = "", password = "")
    } else {
        mainViewModel.getPasswordById(passwordId) ?: return // Return if password not found
    }

    var title by remember { mutableStateOf(password.title) }
    var username by remember { mutableStateOf(password.username) }
    var passwordValue by remember { mutableStateOf(password.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isNewPassword) "Add Password" else "Edit Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNewPassword) {
                        IconButton(onClick = {
                            mainViewModel.deletePassword(password)
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
                        val updatedPassword = password.copy(
                            title = title,
                            username = username,
                            password = passwordValue
                        )
                        mainViewModel.savePassword(updatedPassword)
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Save")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (e.g., Google, Facebook)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.VisibilityOff
                    else
                        Icons.Filled.Visibility

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
        }
    }
}