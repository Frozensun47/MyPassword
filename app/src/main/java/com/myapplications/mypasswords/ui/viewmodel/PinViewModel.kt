// FILE: com/myapplications/mypasswords/ui/viewmodel/PinViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.repository.PasswordRepository // Import the repository
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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

class PinViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "PinViewModel"

    companion object {
        const val PIN_LENGTH = 6
    }

    private val securityManager = SecurityManager()

    private val _uiState = MutableStateFlow(PinScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var currentMode: PinMode = PinMode.AUTHENTICATE
    private var tempPin: String = ""

    init {
        Log.d(TAG, "init: ViewModel created. Checking lockout status.")
        checkLockoutStatus()
    }

    fun initialize(mode: PinMode) {
        Log.d(TAG, "initialize: Setting mode to $mode")
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
        Log.v(TAG, "onPinDigit: Entered digit. Current PIN length: ${_uiState.value.enteredPin.length + 1}")

        _uiState.update { it.copy(enteredPin = it.enteredPin + digit) }

        if (_uiState.value.enteredPin.length == PIN_LENGTH) {
            Log.d(TAG, "onPinDigit: PIN length reached. Processing PIN.")
            processPin()
        }
    }

    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            Log.v(TAG, "onBackspace: Deleting last digit.")
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1)) }
        }
    }

    fun clearError() {
        Log.d(TAG, "clearError: Clearing error state.")
        _uiState.update { it.copy(showError = false) }
    }

    fun checkLockoutStatus() {
        Log.d(TAG, "checkLockoutStatus: Checking for active lockout.")
        viewModelScope.launch(Dispatchers.IO) {
            val isLockedOut = securityManager.isLockedOut(getApplication())
            val lockoutEndTime = if (isLockedOut) securityManager.getLockoutTimestamp(getApplication()) else 0L

            if (isLockedOut) {
                Log.w(TAG, "checkLockoutStatus: Device is locked out until $lockoutEndTime.")
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLockedOut = true,
                            lockoutTimestamp = lockoutEndTime,
                            enteredPin = ""
                        )
                    }
                }
                while (System.currentTimeMillis() < lockoutEndTime) {
                    val remaining = lockoutEndTime - System.currentTimeMillis()
                    Log.v(TAG, "checkLockoutStatus: Lockout countdown: ${remaining / 1000}s")
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(lockoutTimeRemaining = remaining / 1000L) }
                    }
                    delay(1000L)
                }
                Log.d(TAG, "checkLockoutStatus: Lockout period has ended. Re-checking status.")
                checkLockoutStatus()
            } else {
                Log.d(TAG, "checkLockoutStatus: Device is not locked out.")
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
        Log.d(TAG, "processPin: Starting PIN processing for mode: $currentMode")
        viewModelScope.launch(Dispatchers.IO) {
            when (currentMode) {
                PinMode.SETUP -> handlePinSetup()
                PinMode.AUTHENTICATE, PinMode.VERIFY -> handlePinVerification()
            }
        }
    }

    private suspend fun handlePinSetup() {
        // (This function remains unchanged)
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
                PasswordRepository.awaitInitialization() // Ensure DB is ready before writing
                securityManager.savePin(getApplication(), enteredPin)
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
        // CORRECTED: Explicitly wait for the repository to be initialized.
        // This serializes the database access and prevents the race condition.
        PasswordRepository.awaitInitialization()

        Log.d(TAG, "handlePinVerification: Verifying PIN on IO thread.")
        val isCorrect = securityManager.verifyPin(getApplication(), _uiState.value.enteredPin)

        if (isCorrect) {
            Log.d(TAG, "handlePinVerification: PIN verification successful.")
            withContext(Dispatchers.Main) {
                Log.d(TAG, "handlePinVerification: Updating UI state to isSuccess=true on Main thread.")
                _uiState.update { it.copy(isSuccess = true) }
            }
        } else {
            Log.w(TAG, "handlePinVerification: PIN verification failed.")
            checkLockoutStatus()
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
