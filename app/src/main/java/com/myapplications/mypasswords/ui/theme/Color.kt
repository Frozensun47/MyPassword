// File: ui/theme/Color.kt
package com.myapplications.mypasswords.ui.theme

import androidx.compose.ui.graphics.Color

// 🔷 Primary Colors (Cyber Blue)
val CyberPrimary = Color(0xFF00B0FF)           // Bright cyan-blue
val CyberOnPrimary = Color(0xFF003659)         // Dark text on primary
val CyberPrimaryContainer = Color(0xFF005EA2)  // Deep navy for buttons/cards

// 🌑 Dark Theme
val CyberDarkBackground = Color(0xFF0A0F1F)    // Deep space blue
val CyberDarkSurface = Color(0xFF11182A)       // Card-like surface
val CyberDarkOnSurface = Color(0xFFE6F0FF)     // Glowing soft white text

// 🌕 Light Theme
val CyberLightBackground = Color(0xFFF8FAFC)   // Clean off-white
val CyberLightSurface = Color(0xFFFFFFFF)      // Pure white cards
val CyberLightOnSurface = Color(0xFF121B2C)    // Soft dark text
val CyberLightSecondary = Color(0xFF5C6B80)    // Muted blue-gray

// ⚠️ Error & Success
val CyberError = Color(0xFFFF4D6D)             // Soft red
val CyberSuccess = Color(0xFF00C853)           // Vibrant green

// 🌟 Accent & Glow (for highlights)
val CyberAccent = Color(0xFF00E5FF)            // Neon cyan glow
val CyberGlow = CyberPrimary.copy(alpha = 0.2f) // For subtle effects