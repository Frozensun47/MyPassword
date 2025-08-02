// FILE: com/myapplications/mypasswords/ui/viewmodel/MainViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.HomeItem
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Get the singleton instance of the repository and initialize it.
    private val repository = PasswordRepository.apply {
        initialize(application)
    }

    // Combine folders and root passwords into a single list of HomeItems
    val homeItems: Flow<List<HomeItem>> = combine(
        repository.getAllFolders(),
        repository.getRootPasswords()
    ) { folders, passwords ->
        val folderItems = folders.map { HomeItem.FolderItem(it) }
        val passwordItems = passwords.map { HomeItem.PasswordItem(it) }
        folderItems + passwordItems
    }

    fun getPasswordsInFolder(folderId: String): Flow<List<Password>> {
        return repository.getPasswordsInFolder(folderId)
    }

    fun getAllFolders(): Flow<List<Folder>> = repository.getAllFolders()

    fun savePassword(password: Password) = viewModelScope.launch {
        repository.savePassword(password)
    }

    fun deletePassword(password: Password) = viewModelScope.launch {
        repository.deletePassword(password)
    }

    suspend fun getPasswordById(id: String): Password? {
        return repository.getPasswordById(id)
    }

    fun saveFolder(folder: Folder) = viewModelScope.launch {
        repository.saveFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        repository.deleteFolder(folder)
    }

    fun movePasswordToFolder(password: Password, folderId: String?) = viewModelScope.launch {
        val updatedPassword = password.copy(folderId = folderId)
        repository.updatePassword(updatedPassword)
    }
    fun movePasswordsToFolder(passwordIds: Set<String>, folderId: String?) = viewModelScope.launch {
        passwordIds.forEach { id ->
            val password = repository.getPasswordById(id)
            password?.let {
                val updatedPassword = it.copy(folderId = folderId)
                repository.updatePassword(updatedPassword)
            }
        }
    }
    // Add these two functions to your MainViewModel.kt file

    fun deleteItems(items: Set<HomeItem>) = viewModelScope.launch {
        items.forEach { item ->
            when (item) {
                is HomeItem.FolderItem -> repository.deleteFolder(item.folder)
                is HomeItem.PasswordItem -> repository.deletePassword(item.password)
            }
        }
    }

    fun moveItemsToFolder(items: Set<HomeItem>, folderId: String?) = viewModelScope.launch {
        items.forEach { item ->
            if (item is HomeItem.PasswordItem) {
                val updatedPassword = item.password.copy(folderId = folderId)
                repository.updatePassword(updatedPassword)
            }
        }
    }

    // Add these two functions to your MainViewModel.kt file

    fun movePasswordsToFolderByIds(passwordIds: Set<String>, folderId: String?) = viewModelScope.launch {
        passwordIds.forEach { id ->
            val password = repository.getPasswordById(id)
            password?.let {
                val updatedPassword = it.copy(folderId = folderId)
                repository.updatePassword(updatedPassword)
            }
        }
    }

    fun deletePasswordsByIds(passwordIds: Set<String>) = viewModelScope.launch {
        passwordIds.forEach { id ->
            val password = repository.getPasswordById(id)
            password?.let {
                repository.deletePassword(it)
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }
}
