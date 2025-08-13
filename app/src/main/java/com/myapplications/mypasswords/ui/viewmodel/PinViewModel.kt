// FILE: com/myapplications/mypasswords/ui/viewmodel/PinViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the PIN screen.
 */
data class PinScreenUiState(
    val title: String = "",
    val subtitle: String = "",
    val enteredPin: String = "",
    val showError: Boolean = false,
    val isSuccess: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutTimestamp: Long = 0L,
    val lockoutTimeRemaining: Long = 0L
)

/**
 * ViewModel for handling the logic of the PIN entry screen, including setup,
 * authentication, and temporary lockouts.
 */
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
        // Check for any existing lockout as soon as the screen is displayed.
        checkLockoutStatus()
    }

    /**
     * Sets the initial mode for the PIN screen (e.g., setting a new PIN vs. authenticating).
     */
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

    /**
     * Appends a digit to the current PIN entry. Processes the PIN if it reaches the required length.
     */
    fun onPinDigit(digit: String) {
        if (_uiState.value.isLockedOut || _uiState.value.enteredPin.length >= PIN_LENGTH) return

        _uiState.update { it.copy(enteredPin = it.enteredPin + digit) }

        if (_uiState.value.enteredPin.length == PIN_LENGTH) {
            processPin()
        }
    }

    /**
     * Removes the last digit from the PIN entry.
     */
    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1)) }
        }
    }

    /**
     * Resets the error state, typically used to hide an error message after a delay.
     */
    fun clearError() {
        _uiState.update { it.copy(showError = false) }
    }

    /**
     * Checks if the user is currently locked out due to too many failed PIN attempts.
     * If locked out, it starts a countdown timer.
     */
    private fun checkLockoutStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val isLockedOut = securityManager.isLockedOut(getApplication())
            val lockoutEndTime = if (isLockedOut) securityManager.getLockoutTimestamp(getApplication()) else 0L

            if (isLockedOut && System.currentTimeMillis() < lockoutEndTime) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(isLockedOut = true, lockoutTimestamp = lockoutEndTime, enteredPin = "")
                    }
                }
                // Start a countdown timer and update the UI state.
                while (System.currentTimeMillis() < lockoutEndTime) {
                    val remaining = lockoutEndTime - System.currentTimeMillis()
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(lockoutTimeRemaining = remaining / 1000L) }
                    }
                    delay(1000L)
                }
            }
            // Once the delay is over, or if never locked out, ensure the screen is unlocked.
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(isLockedOut = false, lockoutTimestamp = 0L, lockoutTimeRemaining = 0L)
                }
            }
        }
    }

    /**
     * Processes the entered PIN based on the current mode (Setup or Authenticate).
     */
    private fun processPin() {
        viewModelScope.launch(Dispatchers.IO) {
            when (currentMode) {
                PinMode.SETUP -> handlePinSetup()
                PinMode.AUTHENTICATE, PinMode.VERIFY -> handlePinVerification()
            }
        }
    }

    /**
     * Handles the two-step process of setting up a new PIN.
     */
    private suspend fun handlePinSetup() {
        val enteredPin = _uiState.value.enteredPin
        if (tempPin.isEmpty()) {
            // First entry: store the PIN temporarily and prompt for confirmation.
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
            // Second entry: check if it matches the first.
            if (tempPin == enteredPin) {
                PasswordRepository.awaitInitialization() // Ensure DB is ready before writing.
                securityManager.savePin(getApplication(), enteredPin)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSuccess = true) }
                }
            } else {
                // PINs do not match; reset the process.
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

    /**
     * Verifies the entered PIN against the stored one.
     */
    private suspend fun handlePinVerification() {
        // Explicitly wait for the repository to be initialized to prevent race conditions.
        PasswordRepository.awaitInitialization()

        val isCorrect = securityManager.verifyPin(getApplication(), _uiState.value.enteredPin)

        if (isCorrect) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isSuccess = true) }
            }
        } else {
            // If incorrect, check if a lockout should be triggered.
            checkLockoutStatus()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(enteredPin = "", showError = true) }
            }
        }
    }

    /**
     * Defines the different modes the PIN screen can operate in.
     */
    enum class PinMode(val title: String, val subtitle: String) {
        SETUP("Set Your PIN", "Create a 6-digit PIN for security."),
        AUTHENTICATE("Welcome Back", "Enter your PIN to unlock."),
        VERIFY("Security Check", "Enter your PIN to continue.")
    }
}
