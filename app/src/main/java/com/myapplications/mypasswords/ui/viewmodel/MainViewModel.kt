package com.myapplications.mypasswords.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the initial state of the app (PIN set or not)
enum class AppState {
    LOADING,
    PIN_NOT_SET,
    PIN_SET
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val securityManager = SecurityManager(application)

    private val _appState = MutableStateFlow(AppState.LOADING)
    val appState = _appState.asStateFlow()

    init {
        checkInitialState()
    }

    /**
     * Checks if a PIN is set and updates the UI state accordingly.
     * This is called from a coroutine using viewModelScope.
     */
    private fun checkInitialState() {
        viewModelScope.launch {
            if (securityManager.isPinSet()) {
                _appState.value = AppState.PIN_SET
            } else {
                _appState.value = AppState.PIN_NOT_SET
            }
        }
    }

    /**
     * Example function to verify a PIN submitted by the user.
     */
    fun onPinSubmitted(pin: String) {
        viewModelScope.launch {
            val isCorrect = securityManager.verifyPin(pin)
            if (isCorrect) {
                // PIN is correct, navigate to main content
            } else {
                // Show "incorrect PIN" error
            }
        }
    }
}