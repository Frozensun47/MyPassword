package com.myapplications.mypasswords.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// New Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = SteelBlue,
    secondary = SkyBlue,
    background = DeepBlue,
    surface = SlateBlue,
    onPrimary = LightGrey,
    onSecondary = LightGrey,
    onBackground = LightGrey,
    onSurface = LightGrey
)

// New Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = SteelBlue,
    secondary = SkyBlue,
    background = PureWhite,
    surface = LightGrey,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = DeepBlue,
    onSurface = DeepBlue
)

@Composable
fun MyPasswordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // This is the key change: Make status bar match the background
            window.statusBarColor = colorScheme.background.toArgb()
            // This ensures status bar icons (time, battery) are visible
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}