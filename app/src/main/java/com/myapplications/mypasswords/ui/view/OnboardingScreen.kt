package com.myapplications.mypasswords.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Lock,
            title = "Ultimate Security",
            description = "Your data is secured with AES-256, one of the strongest encryption standards. It never leaves your device."
        ),
        OnboardingPage(
            icon = Icons.Default.PhonelinkLock,
            title = "Strictly Offline",
            description = "This app works completely offline. There are no servers, no cloud sync, and no internet access required."
        ),
        OnboardingPage(
            icon = Icons.Default.Warning,
            title = "Your Responsibility",
            description = "Because there are no backups, if you forget your PIN or lose your phone, your data is gone forever. Your PIN cannot be changed."
        )
    )

    Scaffold(
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(12.dp)
                        )
                    }
                }
                Button(
                    onClick = { navController.navigate(Screen.PinSetup.route) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("I Understand, Continue")
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(paddingValues)
        ) { page ->
            OnboardingCard(page = pages[page])
        }
    }
}

@Composable
fun OnboardingCard(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = page.icon, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = page.description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

data class OnboardingPage(val icon: ImageVector, val title: String, val description: String)