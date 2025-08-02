// FILE: com/myapplications/mypasswords/ui/view/PasswordDetailScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    navController: NavController,
    passwordId: String?,
    folderId: String?,
    mainViewModel: MainViewModel = viewModel()
) {
    val isNewPassword = passwordId == "new" || passwordId == null

    var password by remember { mutableStateOf<Password?>(null) }
    var isLoading by remember { mutableStateOf(!isNewPassword) }

    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isFormValid = title.isNotBlank() && username.isNotBlank() && passwordValue.isNotBlank()

    LaunchedEffect(passwordId) {
        if (!isNewPassword) {
            password = mainViewModel.getPasswordById(passwordId!!)
            password?.let {
                title = it.title
                username = it.username
                passwordValue = it.password
            }
            isLoading = false
        }
    }

    fun handleSave() {
        val passwordToSave = if (isNewPassword) {
            Password(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                username = username.trim(),
                password = passwordValue,
                folderId = folderId
            )
        } else {
            password!!.copy(
                title = title.trim(),
                username = username.trim(),
                password = passwordValue
            )
        }
        mainViewModel.savePassword(passwordToSave)
        navController.popBackStack()
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
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFormValid,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = ::handleSave) {
                    Icon(Icons.Default.Check, contentDescription = "Save Password")
                }
            }
        }
    ) { paddingValues ->
        if (isLoading && !isNewPassword) {
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
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
            }
        }
    }
}
