package com.myapplications.mypasswords.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PasswordRepository

    fun getPasswords(): Flow<List<Password>> {
        return repository.getPasswords()
    }

    fun getPassword(id: String?): Password? {
        return repository.getPassword(id)
    }

    fun savePassword(password: Password) {
        viewModelScope.launch {
            repository.savePassword(password)
        }
    }

    fun deletePassword(password: Password) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }

    // --- NEW FUNCTION: Update Folder ---
    fun updatePasswordFolder(passwordId: String, folderName: String) {
        viewModelScope.launch {
            val password = repository.getPassword(passwordId)
            password?.let {
                val updatedPassword = it.copy(folder = folderName.trim())
                repository.savePassword(updatedPassword)
            }
        }
    }

    // --- NEW FUNCTION: Update Color ---
    fun updatePasswordColor(passwordId: String, colorHex: String) {
        viewModelScope.launch {
            val password = repository.getPassword(passwordId)
            password?.let {
                // Use null if the color is white, to signify "default"
                val finalColorHex = if (colorHex == "#FFFFFF") null else colorHex
                val updatedPassword = it.copy(colorHex = finalColorHex)
                repository.savePassword(updatedPassword)
            }
        }
    }
}