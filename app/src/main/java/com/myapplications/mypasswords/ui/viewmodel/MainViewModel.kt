package com.myapplications.mypasswords.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.Flow

class MainViewModel : ViewModel() {

    private val passwordRepository = PasswordRepository

    fun getPasswords(): Flow<List<Password>> {
        return passwordRepository.passwords
    }

    fun getPassword(id: String?): Password? {
        return passwordRepository.getPassword(id)
    }

    fun savePassword(password: Password) {
        passwordRepository.savePassword(password)
    }

    fun deletePassword(password: Password) {
        passwordRepository.deletePassword(password)
    }

    fun deleteAllData(context: Context) {
        SecurityManager(context).deleteAllData()
        // Also need to clear the repository's state
        PasswordRepository.initialize(context)
    }
}