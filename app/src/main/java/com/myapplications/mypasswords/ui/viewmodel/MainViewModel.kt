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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

// Data class to represent the UI state, including a loading flag.
data class MainScreenUiState(
    val isLoading: Boolean = true,
    val homeItems: List<HomeItem> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PasswordRepository

    // Private MutableStateFlow to hold the UI state.
    private val _uiState = MutableStateFlow(MainScreenUiState())
    // Publicly exposed as a regular StateFlow for the UI to observe.
    val uiState = _uiState.asStateFlow()

    private val hasLoadedData = AtomicBoolean(false)

    fun loadData() {
        // Only load data if it hasn't been loaded before.
        if (hasLoadedData.compareAndSet(false, true)) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                // CORRECTED: Fetch initial data sequentially to prevent database contention.
                val folders = repository.getAllFolders().flowOn(Dispatchers.IO).first()
                val passwordEntries = repository.getRootEntriesWithCredentials().flowOn(Dispatchers.IO).first()

                val folderItems = folders.map { HomeItem.FolderItem(it) }
                val passwordItems = passwordEntries.map { HomeItem.PasswordEntryItem(it) }
                val combinedItems = folderItems + passwordItems

                // Now that all data is fetched, update the UI state.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        homeItems = combinedItems
                    )
                }

                // OPTIONAL: If you need live updates after the initial load,
                // you could start a new, combined flow collection here.
                // For fixing the startup issue, this sequential load is sufficient.
            }
        }
    }

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
