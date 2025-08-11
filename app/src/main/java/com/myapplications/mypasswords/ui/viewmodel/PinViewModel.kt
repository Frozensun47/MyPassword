// FILE: com/myapplications/mypasswords/ui/viewmodel/PinViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// UI state now includes lockout information and the lockout end time
data class PinScreenUiState(
    val title: String = "",
    val subtitle: String = "",
    val enteredPin: String = "",
    val showError: Boolean = false,
    val isSuccess: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutTimestamp: Long = 0L, // Timestamp for when the lockout ends
    val lockoutTimeRemaining: Long = 0L // New field for the time remaining in the countdown
)

class PinViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val PIN_LENGTH = 6
    }

    private val securityManager = SecurityManager()

    private val _uiState = MutableStateFlow(PinScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var currentMode: PinMode = PinMode.AUTHENTICATE
    private var tempPin: String = ""

    init {
        // Initial check for lockout as soon as the ViewModel is created
        checkLockoutStatus()
    }

    fun initialize(mode: PinMode) {
        currentMode = mode
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

    fun onPinDigit(digit: String) {
        if (_uiState.value.isLockedOut || _uiState.value.enteredPin.length >= PIN_LENGTH) return

        _uiState.update { it.copy(enteredPin = it.enteredPin + digit) }

        if (_uiState.value.enteredPin.length == PIN_LENGTH) {
            processPin()
        }
    }

    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1)) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(showError = false) }
    }

    /**
     * Checks the current lockout status and updates the UI state.
     * If a lockout is active, it starts a coroutine to wait for the lockout to end.
     */
    fun checkLockoutStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val isLockedOut = securityManager.isLockedOut(getApplication())
            val lockoutEndTime = if (isLockedOut) securityManager.getLockoutTimestamp(getApplication()) else 0L

            if (isLockedOut) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLockedOut = true,
                            lockoutTimestamp = lockoutEndTime,
                            enteredPin = ""
                        )
                    }
                }
                // Start a countdown timer and update the UI state
                while (System.currentTimeMillis() < lockoutEndTime) {
                    val remaining = lockoutEndTime - System.currentTimeMillis()
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(lockoutTimeRemaining = remaining / 1000L) }
                    }
                    delay(1000L)
                }
                // Once the delay is over, re-check the status to unlock the screen
                checkLockoutStatus()
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLockedOut = false,
                            lockoutTimestamp = 0L,
                            lockoutTimeRemaining = 0L
                        )
                    }
                }
            }
        }
    }

    private fun processPin() {
        viewModelScope.launch(Dispatchers.IO) {
            when (currentMode) {
                PinMode.SETUP -> handlePinSetup()
                PinMode.AUTHENTICATE, PinMode.VERIFY -> handlePinVerification()
            }
        }
    }

    private suspend fun handlePinSetup() {
        val enteredPin = _uiState.value.enteredPin
        if (tempPin.isEmpty()) {
            tempPin = enteredPin
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        title = "Confirm Your PIN",
                        subtitle = "Re-enter the PIN to confirm.",
                        enteredPin = ""
                    )
                }
            }
        } else {
            if (tempPin == enteredPin) {
                withContext(Dispatchers.IO) {
                    securityManager.savePin(getApplication(), enteredPin)
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSuccess = true) }
                }
            } else {
                tempPin = ""
                withContext(Dispatchers.Main) {
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
    }

    private suspend fun handlePinVerification() {
        val isCorrect = withContext(Dispatchers.IO) {
            securityManager.verifyPin(getApplication(),_uiState.value.enteredPin)
        }
        if (isCorrect) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isSuccess = true) }
            }
        } else {
            // Verification failed, check if we are now locked out
            checkLockoutStatus()
            // Clear the pin and show an error state for a moment
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(enteredPin = "", showError = true) }
            }
        }
    }

    enum class PinMode(val title: String, val subtitle: String) {
        SETUP("Set Your PIN", "Create a 6-digit PIN for security."),
        AUTHENTICATE("Welcome Back", "Enter your PIN to unlock."),
        VERIFY("Security Check", "Enter your PIN to continue.")
    }
}
