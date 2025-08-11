// FILE: com/myapplications/mypasswords/ui/viewmodel/MainViewModel.kt
package com.myapplications.mypasswords.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.HomeItem
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.model.PasswordEntryWithCredentials
import com.myapplications.mypasswords.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // The repository is now initialized in the Application class, so this call is removed.
    private val repository = PasswordRepository

    // Combine folders and root password entries into a single list for the main screen.
    val homeItems: Flow<List<HomeItem>> = combine(
        repository.getAllFolders().flowOn(Dispatchers.Default),
        repository.getRootEntriesWithCredentials().flowOn(Dispatchers.Default)
    ) { folders, passwordEntries ->
        val folderItems = folders.map { HomeItem.FolderItem(it) }
        val passwordItems = passwordEntries.map { HomeItem.PasswordEntryItem(it) }
        // You can add sorting logic here if needed, e.g., .sortedBy { it.title }
        folderItems + passwordItems
    }

    // --- Password Entry Functions ---

    fun getEntriesInFolder(folderId: String): Flow<List<PasswordEntryWithCredentials>> {
        return repository.getEntriesInFolder(folderId)
    }

    fun getEntryWithCredentials(entryId: String): Flow<PasswordEntryWithCredentials?> {
        return repository.getEntryWithCredentials(entryId)
    }

    fun saveEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) = viewModelScope.launch {
        repository.saveEntryWithCredentials(entry, credentials)
    }

    fun deleteEntry(entry: PasswordEntry) = viewModelScope.launch {
        repository.deleteEntry(entry)
    }


    // --- Unified Functions for Selection Mode ---

    fun deleteItems(items: Set<HomeItem>) = viewModelScope.launch {
        items.forEach { item ->
            when (item) {
                is HomeItem.FolderItem -> repository.deleteFolder(item.folder)
                is HomeItem.PasswordEntryItem -> repository.deleteEntry(item.entryWithCredentials.entry)
            }
        }
    }

    fun moveItemsToFolder(items: Set<HomeItem>, folderId: String?) = viewModelScope.launch {
        items.forEach { item ->
            if (item is HomeItem.PasswordEntryItem) {
                val updatedEntry = item.entryWithCredentials.entry.copy(folderId = folderId)
                repository.updateEntry(updatedEntry)
            }
        }
    }

    // --- Folder Management ---

    fun getAllFolders(): Flow<List<Folder>> = repository.getAllFolders()

    fun saveFolder(folder: Folder) = viewModelScope.launch {
        repository.saveFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        repository.deleteFolder(folder)
    }

    // --- General ---

    fun deleteAllData() = viewModelScope.launch {
        repository.deleteAllData()
    }
}
