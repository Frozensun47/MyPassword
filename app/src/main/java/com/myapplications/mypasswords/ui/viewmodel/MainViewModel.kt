package com.myapplications.mypasswords.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.repository.PasswordRepository
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.Flow

class MainViewModel : ViewModel() {

    private val passwordRepository = PasswordRepository()

    fun getPasswords(context: Context): Flow<List<Password>> {
        return passwordRepository.getPasswords(context)
    }

    fun getPassword(context: Context, id: String?): Password? {
    return passwordRepository.getPassword(context, id)
    }

    fun savePassword(context: Context, password: Password) {
        passwordRepository.savePassword(context, password)
    }

    fun deletePassword(context: Context, password: Password) {
        passwordRepository.deletePassword(context, password)
    }
    fun deleteAllData(context: Context) {
        SecurityManager(context).deleteAllData()
    }
}