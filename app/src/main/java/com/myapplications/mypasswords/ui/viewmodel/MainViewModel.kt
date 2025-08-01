package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Get the singleton instance of the repository and initialize it.
    private val repository = PasswordRepository.apply {
        initialize(application)
    }

    /**
     * Exposes the Flow of passwords from the repository to be collected by the UI.
     */
    fun getPasswords(): Flow<List<Password>> {
        return repository.getPasswords()
    }


    /**
     * Fetches a single password by its ID from the repository.
     * This is a suspend function and must be called from a coroutine.
     */
    fun getPasswordById(id: String): Password? {
        return repository.getPassword(id)
    }

    /**
     * Saves a new password or updates an existing one.
     */
    fun savePassword(password: Password) {
        viewModelScope.launch {
            repository.savePassword(password)
        }
    }

    /**
     * Deletes a given password.
     */
    fun deletePassword(password: Password) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }

    /**
     * Updates the folder name for a specific password.
     * The operation is launched in a coroutine.
     */
    fun updatePasswordFolder(passwordId: String, folderName: String) {
        viewModelScope.launch {
            val password = repository.getPassword(passwordId)
            password?.let {
                // Set folder to null if the input is blank, otherwise use the trimmed name
                val finalFolderName = folderName.trim().takeIf { it.isNotBlank() }
                val updatedPassword = it.copy(folder = finalFolderName)
                repository.savePassword(updatedPassword)
            }
        }
    }

    /**
     * Updates the color hex for a specific password.
     * The operation is launched in a coroutine.
     */
    fun updatePasswordColor(passwordId: String, colorHex: String) {
        viewModelScope.launch {
            val password = repository.getPassword(passwordId)
            password?.let {
                // Use null if the color is white, to signify "default"
                val finalColorHex = if (colorHex.equals("#FFFFFF", ignoreCase = true)) null else colorHex
                val updatedPassword = it.copy(colorHex = finalColorHex)
                repository.savePassword(updatedPassword)
            }
        }
    }

    /**
     * Deletes all passwords from the database.
     * This is the new function you requested.
     */
    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }
}