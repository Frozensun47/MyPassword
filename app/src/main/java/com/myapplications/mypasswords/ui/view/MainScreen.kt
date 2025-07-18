package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.myapplications.mypasswords.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
val context = LocalContext.current
val passwords = mainViewModel.getPasswords(context).collectAsState(initial = emptyList())

Scaffold(
        topBar = {
    TopAppBar(
            title = { Text("MyPasswords") }
    )
},
floatingActionButton = {
FloatingActionButton(onClick = { navController.navigate("password_detail/new") }) {
Icon(Icons.Default.Add, contentDescription = "Add Password")
            }
                    }
                    ) { padding ->
LazyColumn(modifier = Modifier.padding(padding)) {
items(passwords.value) { password ->
        Card(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { navController.navigate("password_detail/${password.id}") }
                ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(password.title, style = MaterialTheme.typography.headlineSmall)
            Text(password.username)
        }
    }
}
        }
                }
                }