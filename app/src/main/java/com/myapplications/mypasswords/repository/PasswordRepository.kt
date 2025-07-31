package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PasswordRepository {

    private val _passwords = MutableStateFlow<List<Password>>(emptyList())
    val passwords: Flow<List<Password>> = _passwords.asStateFlow()

    private lateinit var securityManager: SecurityManager

    fun initialize(context: Context) {
        securityManager = SecurityManager(context)
        _passwords.value = securityManager.getPasswords()
    }

    fun getPassword(id: String?): Password? {
        return _passwords.value.find { it.id == id }
    }

    fun savePassword(password: Password) {
        securityManager.savePassword(password)
        _passwords.value = securityManager.getPasswords()
    }

    fun deletePassword(password: Password) {
        securityManager.deletePassword(password)
        _passwords.value = securityManager.getPasswords()
    }
}