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

/**
 * UI state for the Main screen. Includes a loading flag and an optional error message.
 */
data class MainScreenUiState(
    val isLoading: Boolean = true,
    val homeItems: List<HomeItem> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for the main screen, responsible for loading and managing folders and password entries.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PasswordRepository

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val hasLoadedData = AtomicBoolean(false)

    /**
     * Loads the initial data for the screen and then listens for live updates.
     * This function is designed to be called only once from the UI.
     */
    fun loadData() {
        if (hasLoadedData.compareAndSet(false, true)) {
            viewModelScope.launch {
                try {
                    // Start by showing the loading indicator.
                    _uiState.update { it.copy(isLoading = true) }

                    // --- Step 1: Initial Sequential Load ---
                    // This is the crucial fix for the startup race condition. We fetch the initial
                    // state of the data sequentially to ensure the database is not overwhelmed.
                    val initialFolders = repository.getAllFolders().flowOn(Dispatchers.IO).first()
                    val initialPasswords = repository.getRootEntriesWithCredentials().flowOn(Dispatchers.IO).first()

                    // Immediately display the first set of data.
                    val initialItems = combineAndMap(initialFolders, initialPasswords)
                    _uiState.update { it.copy(isLoading = false, homeItems = initialItems, error = null) }

                    // --- Step 2: Live Updates ---
                    // Now that the initial data is loaded and displayed, we start a long-running
                    // coroutine to listen for any changes in the database.
                    val foldersFlow = repository.getAllFolders()
                    val passwordsFlow = repository.getRootEntriesWithCredentials()

                    combine(foldersFlow, passwordsFlow) { folders, passwordEntries ->
                        combineAndMap(folders, passwordEntries)
                    }
                        .flowOn(Dispatchers.Default) // Perform mapping on a background thread.
                        .catch { e ->
                            // Handle any errors that might occur in the live flow.
                            _uiState.update { it.copy(error = e.message) }
                        }
                        .collect { updatedItems ->
                            // Update the UI with the latest data from the database.
                            _uiState.update { it.copy(homeItems = updatedItems) }
                        }

                } catch (e: Exception) {
                    // Handle any errors from the initial sequential load.
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }

    /**
     * A helper function to combine and map folders and passwords into a single list for the UI.
     */
    private fun combineAndMap(folders: List<Folder>, passwordEntries: List<PasswordEntryWithCredentials>): List<HomeItem> {
        val folderItems = folders.map { HomeItem.FolderItem(it) }
        val passwordItems = passwordEntries.map { HomeItem.PasswordEntryItem(it) }
        return folderItems + passwordItems
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