package com.myapplications.mypasswords.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.myapplications.mypasswords.security.SecurityManager

class PinViewModel : ViewModel() {

    fun isPinSet(context: Context): Boolean {
        return SecurityManager(context).isPinSet()
    }

    fun savePin(context: Context, pin: String) {
        SecurityManager(context).savePin(pin)
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        return SecurityManager(context).verifyPin(pin)
    }
}