// FILE: com/myapplications/mypasswords/ui/viewmodel/PinViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI state now includes lockout information
data class PinScreenUiState(
    val title: String = "",
    val subtitle: String = "",
    val enteredPin: String = "",
    val showError: Boolean = false,
    val isSuccess: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutMessage: String = ""
)

class PinViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val PIN_LENGTH = 6
    }

    private val securityManager = SecurityManager(application)
    private var lockoutTimer: CountDownTimer? = null

    private val _uiState = MutableStateFlow(PinScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var currentMode: PinMode = PinMode.AUTHENTICATE
    private var tempPin: String = ""
    private var onSuccessCallback: () -> Unit = {}

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
        // Check for existing lockout when the screen is first shown
        checkForExistingLockout()
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
            tempPin = enteredPin
            _uiState.update {
                it.copy(
                    title = "Confirm Your PIN",
                    subtitle = "Re-enter the PIN to confirm.",
                    enteredPin = ""
                )
            }
        } else {
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
            // Verification failed, check if we are now locked out
            if (securityManager.isLockedOut()) {
                startLockoutTimer(securityManager.getLockoutTimestamp())
            } else {
                _uiState.update { it.copy(enteredPin = "", showError = true) }
            }
        }
    }

    private fun checkForExistingLockout() {
        viewModelScope.launch {
            if (securityManager.isLockedOut()) {
                startLockoutTimer(securityManager.getLockoutTimestamp())
            }
        }
    }

    private fun startLockoutTimer(lockoutEndTime: Long) {
        lockoutTimer?.cancel()
        val remainingTime = lockoutEndTime - System.currentTimeMillis()
        if (remainingTime > 0) {
            _uiState.update {
                it.copy(
                    isLockedOut = true,
                    enteredPin = ""
                )
            }
            lockoutTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = millisUntilFinished / 1000
                    _uiState.update {
                        it.copy(lockoutMessage = "Too many attempts. Try again in $secondsLeft seconds.")
                    }
                }

                override fun onFinish() {
                    _uiState.update {
                        it.copy(
                            isLockedOut = false,
                            lockoutMessage = "",
                            subtitle = currentMode.subtitle // Restore original subtitle
                        )
                    }
                }
            }.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        lockoutTimer?.cancel() // Prevent memory leaks
    }

    enum class PinMode(val title: String, val subtitle: String) {
        SETUP("Set Your PIN", "Create a 6-digit PIN for security."),
        AUTHENTICATE("Welcome Back", "Enter your PIN to unlock."),
        VERIFY("Security Check", "Enter your PIN to continue.")
    }
}
