// FILE: com/myapplications/mypasswords/ui/viewmodel/PinViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// This data class holds all the information the UI needs to draw itself.
data class PinScreenUiState(
    val title: String = "",
    val subtitle: String = "",
    val enteredPin: String = "",
    val showError: Boolean = false,
    val isSuccess: Boolean = false
)

class PinViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val PIN_LENGTH = 6 // Changed to 6 digits
    }

    private val securityManager = SecurityManager(application)

    private val _uiState = MutableStateFlow(PinScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var currentMode: PinMode = PinMode.AUTHENTICATE
    private var tempPin: String = "" // Used for confirming PIN in setup mode
    private var onSuccessCallback: () -> Unit = {}

    // This is called once when the screen is created.
    fun initialize(mode: PinMode, onSuccess: () -> Unit) {
        currentMode = mode
        onSuccessCallback = onSuccess
        _uiState.update {
            it.copy(
                title = mode.title,
                subtitle = mode.subtitle,
                enteredPin = "",
                showError = false,
                isSuccess = false
            )
        }
    }

    // Called every time a number button is pressed.
    fun onPinDigit(digit: String) {
        if (_uiState.value.enteredPin.length < PIN_LENGTH) {
            _uiState.update { it.copy(enteredPin = it.enteredPin + digit) }

            // If the PIN is now complete, process it.
            if (_uiState.value.enteredPin.length == PIN_LENGTH) {
                processPin()
            }
        }
    }

    // Called when the backspace button is pressed.
    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1)) }
        }
    }

    // Resets the error state (e.g., after the shake animation).
    fun clearError() {
        _uiState.update { it.copy(showError = false) }
    }

    private fun processPin() {
        viewModelScope.launch {
            when (currentMode) {
                PinMode.SETUP -> handlePinSetup()
                PinMode.AUTHENTICATE, PinMode.VERIFY -> handlePinVerification()
            }
        }
    }

    private suspend fun handlePinSetup() {
        val enteredPin = _uiState.value.enteredPin
        if (tempPin.isEmpty()) {
            // First time entering the PIN
            tempPin = enteredPin
            _uiState.update {
                it.copy(
                    title = "Confirm Your PIN",
                    subtitle = "Re-enter the PIN to confirm.",
                    enteredPin = ""
                )
            }
        } else {
            // Second time, confirming the PIN
            if (tempPin == enteredPin) {
                securityManager.savePin(enteredPin)
                _uiState.update { it.copy(isSuccess = true) }
            } else {
                tempPin = ""
                _uiState.update {
                    it.copy(
                        title = "PINs Don't Match",
                        subtitle = "Please try setting your PIN again.",
                        enteredPin = "",
                        showError = true
                    )
                }
            }
        }
    }

    private suspend fun handlePinVerification() {
        if (securityManager.verifyPin(_uiState.value.enteredPin)) {
            _uiState.update { it.copy(isSuccess = true) }
        } else {
            _uiState.update { it.copy(enteredPin = "", showError = true) }
        }
    }

    // The PinMode enum should be defined here, within the ViewModel that uses it.
    enum class PinMode(val title: String, val subtitle: String) {
        SETUP("Set Your PIN", "Create a 6-digit PIN for security."), // Updated text
        AUTHENTICATE("Welcome Back", "Enter your PIN to unlock."),
        VERIFY("Security Check", "Enter your PIN to continue.")
    }
}
