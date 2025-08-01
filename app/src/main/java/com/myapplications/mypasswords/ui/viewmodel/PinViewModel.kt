package com.myapplications.mypasswords.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinViewModel : ViewModel() {

    // Define the PinMode enum right here
    enum class PinMode(val title: String, val subtitle: String) {
        SETUP("Set Your PIN", "Create a 4-8 digit PIN. This cannot be changed later."),
        AUTHENTICATE("Welcome Back", "Enter your PIN to unlock the app."),
        VERIFY("Security Check", "Re-enter your PIN to continue.")
    }

    private val _pinState = MutableStateFlow(PinScreenState())
    val pinState = _pinState.asStateFlow()

    fun onPinChanged(newPin: String) {
        if (newPin.length <= 8 && newPin.all { it.isDigit() }) {
            _pinState.update { it.copy(pin = newPin, errorMessage = null) }
        }
    }

    suspend fun checkInitialLockout(context: Context) {
        val securityManager = SecurityManager(context)
        if (securityManager.isLockedOut()) {
            _pinState.update {
                it.copy(
                    isLocked = true,
                    lockoutUntil = securityManager.getLockoutTimestamp()
                )
            }
        }
    }

    suspend fun clearLockoutIfExpired(context: Context) {
        val securityManager = SecurityManager(context)
        if (!securityManager.isLockedOut()) {
            _pinState.update { it.copy(isLocked = false, lockoutUntil = 0L) }
        }
    }


    fun submitPin(context: Context, mode: PinMode, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val securityManager = SecurityManager(context)
            val currentPin = _pinState.value.pin

            // Validate PIN format for setup
            if (mode == PinMode.SETUP && !securityManager.validatePinFormat(currentPin)) {
                _pinState.update { it.copy(errorMessage = "PIN must be 4-8 digits.") }
                return@launch
            }

            // Process PIN based on mode
            when (mode) {
                PinMode.SETUP -> {
                    securityManager.savePin(currentPin)
                    onSuccess()
                }
                PinMode.AUTHENTICATE, PinMode.VERIFY -> {
                    if (securityManager.verifyPin(currentPin)) {
                        onSuccess()
                    } else {
                        // Check for lockout after failed attempt
                        if (securityManager.isLockedOut()) {
                            _pinState.update {
                                it.copy(
                                    pin = "",
                                    errorMessage = "Too many failed attempts.",
                                    isLocked = true,
                                    lockoutUntil = securityManager.getLockoutTimestamp()
                                )
                            }
                        } else {
                            _pinState.update { it.copy(pin = "", errorMessage = "Incorrect PIN. Please try again.") }
                        }
                    }
                }
            }
        }
    }
}

data class PinScreenState(
    val pin: String = "",
    val errorMessage: String? = null,
    val isLocked: Boolean = false,
    val lockoutUntil: Long = 0L
)