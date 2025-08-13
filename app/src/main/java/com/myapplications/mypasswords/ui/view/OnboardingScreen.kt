// FILE: com/myapplications/mypasswords/ui/view/OnboardingScreen.kt
package com.myapplications.mypasswords.ui.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapplications.mypasswords.navigation.Screen
import kotlinx.coroutines.launch

/**
 * Represents a single page in the onboarding flow.
 * @param icon The icon to display for the page.
 * @param title The main title of the page.
 * @param description The detailed description for the page.
 */
data class OnboardingPage(val icon: ImageVector, val title: String, val description: String)

/**
 * The main composable for the onboarding experience. It uses a HorizontalPager to
 * guide the user through the app's key features and security principles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Celebration,
            title = "Welcome to MyPasswords",
            description = "A simple, secure, and completely offline place to store your most important information."
        ),
        OnboardingPage(
            icon = Icons.Default.CloudOff,
            title = "100% Offline & Private",
            description = "This app does not connect to the internet. We collect no data, and your passwords never leave your device."
        ),
        OnboardingPage(
            icon = Icons.Default.VerifiedUser,
            title = "Military-Grade Encryption",
            description = "All of your data is encrypted and secured with AES-256, the same standard used by governments."
        ),
        OnboardingPage(
            icon = Icons.Default.Folder,
            title = "Simple Organization",
            description = "Group your passwords into folders to keep your digital life tidy and easy to manage."
        ),
        OnboardingPage(
            icon = Icons.Default.Warning,
            title = "Your Responsibility",
            description = "Because this app is completely offline, there is no account recovery. If you forget your PIN, your data cannot be recovered."
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    TextButton(onClick = {
                        navController.navigate(Screen.PinSetup.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }) {
                        Text("Skip")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                Row(
                    Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        val width = animateDpAsState(targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp, label = "width animation")
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width.value)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                // Next/Get Started Button
                Button(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                navController.navigate(Screen.PinSetup.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    val buttonText = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next"
                    Text(buttonText)
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
private fun OnboardingCard(page: OnboardingPage) {
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
