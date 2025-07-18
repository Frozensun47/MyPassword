package com.myapplications.mypasswords.ui.view;

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel
import java.util.UUID

@Composable
fun PasswordDetailScreen(
        navController: NavController,
        passwordId: String?,
        mainViewModel: MainViewModel = viewModel()
) {
val context = LocalContext.current
val isNewPassword = passwordId == "new"
val password = if (isNewPassword) {
Password(UUID.randomUUID().toString(), "", "", "")
        } else {
        mainViewModel.getPassword(context, passwordId) ?: return
        }

var title by remember { mutableStateOf(password.title) }
var username by remember { mutableStateOf(password.username) }
var passwordValue by remember { mutableStateOf(password.password) }

Column(
        modifier = Modifier
        .fillMaxSize()
            .padding(16.dp)
    ) {
TextField(
        value = title,
        onValueChange = { title = it },
label = { Text("Title") },
modifier = Modifier.fillMaxWidth()
        )
Spacer(modifier = Modifier.height(16.dp))
TextField(
        value = username,
        onValueChange = { username = it },
label = { Text("Username") },
modifier = Modifier.fillMaxWidth()
        )
Spacer(modifier = Modifier.height(16.dp))
TextField(
        value = passwordValue,
        onValueChange = { passwordValue = it },
label = { Text("Password") },
modifier = Modifier.fillMaxWidth()
        )
Spacer(modifier = Modifier.height(32.dp))
Button(onClick = {
    val updatedPassword = password.copy(
            title = title,
            username = username,
            password = passwordValue
    )
    mainViewModel.savePassword(context, updatedPassword)
    navController.popBackStack()
}) {
Text("Save")
        }
                if (!isNewPassword) {
Spacer(modifier = Modifier.height(16.dp))
Button(onClick = {
    mainViewModel.deletePassword(context, password)
    navController.popBackStack()
}) {
Text("Delete")
            }
                    }
                    }
                    }