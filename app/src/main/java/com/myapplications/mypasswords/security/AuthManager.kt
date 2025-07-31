package com.myapplications.mypasswords.security

import androidx.compose.runtime.mutableStateOf

/**
 * A simple singleton to manage the app's session authentication state.
 */
object AuthManager {
    /**
     * Represents if the user is currently authenticated within the session.
     * This is set to false when the app goes into the background and true
     * after a successful PIN entry.
     */
    val isAuthenticated = mutableStateOf(false)
}