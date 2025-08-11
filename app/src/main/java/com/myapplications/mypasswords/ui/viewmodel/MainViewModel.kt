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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PasswordRepository

    val homeItems: Flow<List<HomeItem>> = flow {
        // Now calling suspend functions from a flow builder
        val foldersFlow = repository.getAllFolders()
        val passwordsFlow = repository.getRootEntriesWithCredentials()

        combine(foldersFlow, passwordsFlow) { folders, passwordEntries ->
            val folderItems = folders.map { HomeItem.FolderItem(it) }
            val passwordItems = passwordEntries.map { HomeItem.PasswordEntryItem(it) }
            folderItems + passwordItems
        }.collect { emit(it) }
    }.flowOn(Dispatchers.Default)

    fun getEntriesInFolder(folderId: String): Flow<List<PasswordEntryWithCredentials>> {
        return flow {
            repository.getEntriesInFolder(folderId).collect {
                emit(it)
            }
        }.flowOn(Dispatchers.Default)
    }

    fun getEntryWithCredentials(entryId: String): Flow<PasswordEntryWithCredentials?> {
        return flow {
            repository.getEntryWithCredentials(entryId).collect {
                emit(it)
            }
        }.flowOn(Dispatchers.Default)
    }

    fun saveEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.saveEntryWithCredentials(entry, credentials)
        }
    }

    fun deleteEntry(entry: PasswordEntry) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.deleteEntry(entry)
        }
    }

    fun deleteItems(items: Set<HomeItem>) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            items.forEach { item ->
                when (item) {
                    is HomeItem.FolderItem -> repository.deleteFolder(item.folder)
                    is HomeItem.PasswordEntryItem -> repository.deleteEntry(item.entryWithCredentials.entry)
                }
            }
        }
    }

    fun moveItemsToFolder(items: Set<HomeItem>, folderId: String?) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            items.forEach { item ->
                if (item is HomeItem.PasswordEntryItem) {
                    val updatedEntry = item.entryWithCredentials.entry.copy(folderId = folderId)
                    repository.updateEntry(updatedEntry)
                }
            }
        }
    }

    fun getAllFolders(): Flow<List<Folder>> = flow {
        repository.getAllFolders().collect {
            emit(it)
        }
    }.flowOn(Dispatchers.Default)

    fun saveFolder(folder: Folder) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.saveFolder(folder)
        }
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.deleteFolder(folder)
        }
    }

    fun deleteAllData() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.deleteAllData()
        }
    }
}
