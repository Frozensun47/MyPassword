// FILE: com/myapplications/mypasswords/repository/PasswordRepository.kt
package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.database.DatabaseProvider
import com.myapplications.mypasswords.database.FolderDao
import com.myapplications.mypasswords.database.PasswordEntryDao
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.model.PasswordEntryWithCredentials
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton repository for managing Password Entries, Credentials, and Folders.
 * Handles encryption and decryption of credential data.
 */
object PasswordRepository {

    private lateinit var passwordEntryDao: PasswordEntryDao
    private lateinit var folderDao: FolderDao
    private lateinit var securityManager: SecurityManager

    private val isInitialized = AtomicBoolean(false)
    private lateinit var initializationDeferred: CompletableDeferred<Unit>

    suspend fun initialize(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            initializationDeferred = CompletableDeferred()
            try {
                val appContext = context.applicationContext
                val database = DatabaseProvider.getInstance(appContext)
                passwordEntryDao = database.passwordEntryDao()
                folderDao = database.folderDao()
                securityManager = SecurityManager()
                initializationDeferred.complete(Unit)
            } catch (e: Exception) {
                initializationDeferred.completeExceptionally(e)
                throw e
            }
        } else {
            initializationDeferred.await()
        }
    }

    private suspend fun checkInitialized() {
        if (!isInitialized.get()) {
            initializationDeferred.await()
        }
    }

    // --- Password Entry Functions (with Encryption/Decryption) ---

    private fun decryptCredentials(entry: PasswordEntryWithCredentials): PasswordEntryWithCredentials {
        val decryptedCredentials = entry.credentials.map {
            it.copy(password = securityManager.decrypt(it.password))
        }
        return entry.copy(credentials = decryptedCredentials)
    }

    // Refactored to be suspend functions that return a Flow after initialization check.
    suspend fun getRootEntriesWithCredentials(): Flow<List<PasswordEntryWithCredentials>> {
        checkInitialized()
        return passwordEntryDao.getRootEntriesWithCredentials().map { list ->
            list.map { decryptCredentials(it) }
        }
    }

    suspend fun getEntriesInFolder(folderId: String): Flow<List<PasswordEntryWithCredentials>> {
        checkInitialized()
        return passwordEntryDao.getEntriesWithCredentialsInFolder(folderId).map { list ->
            list.map { decryptCredentials(it) }
        }
    }

    suspend fun getEntryWithCredentials(entryId: String): Flow<PasswordEntryWithCredentials?> {
        checkInitialized()
        return passwordEntryDao.getEntryWithCredentials(entryId).map { entry ->
            entry?.let { decryptCredentials(it) }
        }
    }

    suspend fun saveEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) = withContext(Dispatchers.IO) {
        checkInitialized()
        val encryptedCredentials = credentials.map {
            it.copy(password = securityManager.encrypt(it.password))
        }
        passwordEntryDao.saveEntryWithCredentials(entry, encryptedCredentials)
    }

    suspend fun deleteEntry(entry: PasswordEntry) = withContext(Dispatchers.IO) {
        checkInitialized()
        passwordEntryDao.deleteEntry(entry)
    }

    suspend fun updateEntry(entry: PasswordEntry) = withContext(Dispatchers.IO) {
        checkInitialized()
        passwordEntryDao.updateEntry(entry)
    }

    // --- Folder Functions ---
    // Refactored to be suspend functions that return a Flow after initialization check.
    suspend fun getAllFolders(): Flow<List<Folder>> {
        checkInitialized()
        return folderDao.getAllFolders()
    }

    suspend fun saveFolder(folder: Folder) = withContext(Dispatchers.IO) {
        checkInitialized()
        folderDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) = withContext(Dispatchers.IO) {
        checkInitialized()
        folderDao.deleteFolder(folder)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        checkInitialized()
        passwordEntryDao.deleteAll()
        folderDao.deleteAll()
    }
}
