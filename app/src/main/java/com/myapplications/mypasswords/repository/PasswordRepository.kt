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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Singleton repository for managing Password Entries, Credentials, and Folders.
 * Handles encryption and decryption of credential data.
 */
object PasswordRepository {

    private lateinit var passwordEntryDao: PasswordEntryDao
    private lateinit var folderDao: FolderDao
    private lateinit var securityManager: SecurityManager

    fun initialize(context: Context) {
        if (!::passwordEntryDao.isInitialized) {
            val appContext = context.applicationContext
            val database = DatabaseProvider.getInstance(appContext)
            // Use the new DAO for password entries
            passwordEntryDao = database.passwordEntryDao()
            folderDao = database.folderDao()
            securityManager = SecurityManager()
        }
    }

    // --- Password Entry Functions (with Encryption/Decryption) ---

    private fun decryptCredentials(entry: PasswordEntryWithCredentials): PasswordEntryWithCredentials {
        val decryptedCredentials = entry.credentials.map {
            it.copy(password = securityManager.decrypt(it.password))
        }
        return entry.copy(credentials = decryptedCredentials)
    }

    fun getRootEntriesWithCredentials(): Flow<List<PasswordEntryWithCredentials>> {
        checkInitialized()
        return passwordEntryDao.getRootEntriesWithCredentials().map { list ->
            list.map { decryptCredentials(it) }
        }
    }

    fun getEntriesInFolder(folderId: String): Flow<List<PasswordEntryWithCredentials>> {
        checkInitialized()
        return passwordEntryDao.getEntriesWithCredentialsInFolder(folderId).map { list ->
            list.map { decryptCredentials(it) }
        }
    }

    fun getEntryWithCredentials(entryId: String): Flow<PasswordEntryWithCredentials?> {
        checkInitialized()
        return passwordEntryDao.getEntryWithCredentials(entryId).map { entry ->
            entry?.let { decryptCredentials(it) }
        }
    }

    suspend fun saveEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) {
        checkInitialized()
        val encryptedCredentials = credentials.map {
            it.copy(password = securityManager.encrypt(it.password))
        }
        passwordEntryDao.saveEntryWithCredentials(entry, encryptedCredentials)
    }

    suspend fun deleteEntry(entry: PasswordEntry) {
        checkInitialized()
        passwordEntryDao.deleteEntry(entry)
    }

    suspend fun updateEntry(entry: PasswordEntry) {
        checkInitialized()
        passwordEntryDao.updateEntry(entry)
    }


    // --- Folder Functions ---
    fun getAllFolders(): Flow<List<Folder>> {
        checkInitialized()
        return folderDao.getAllFolders()
    }

    suspend fun saveFolder(folder: Folder) {
        checkInitialized()
        folderDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) {
        checkInitialized()
        folderDao.deleteFolder(folder)
    }

    suspend fun deleteAllData() {
        checkInitialized()
         passwordEntryDao.deleteAll()
         folderDao.deleteAll()
    }

    private fun checkInitialized() {
        if (!::passwordEntryDao.isInitialized) {
            throw IllegalStateException("PasswordRepository not initialized. Call initialize() in your Application class.")
        }
    }
}
