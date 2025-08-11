// FILE: com/myapplications/mypasswords/repository/PasswordRepository.kt
package com.myapplications.mypasswords.repository

import android.content.Context
import android.util.Log
import com.myapplications.mypasswords.database.DatabaseProvider
import com.myapplications.mypasswords.database.FolderDao
import com.myapplications.mypasswords.database.PasswordEntryDao
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.model.PasswordEntryWithCredentials
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton repository for managing Password Entries, Credentials, and Folders.
 * Handles encryption and decryption of credential data.
 */
object PasswordRepository {

    private const val TAG = "PasswordRepository"

    private lateinit var passwordEntryDao: PasswordEntryDao
    private lateinit var folderDao: FolderDao
    private lateinit var securityManager: SecurityManager

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isInitialized = AtomicBoolean(false)
    private val initializationDeferred = CompletableDeferred<Unit>()

    fun initialize(context: Context) {
        Log.d(TAG, "Initialize called. Is already initialized: ${isInitialized.get()}")
        if (isInitialized.compareAndSet(false, true)) {
            Log.d(TAG, "Starting initialization in repositoryScope.")
            repositoryScope.launch {
                try {
                    Log.d(TAG, "Initialization coroutine started on thread: ${Thread.currentThread().name}")
                    val appContext = context.applicationContext
                    val database = DatabaseProvider.getInstance(appContext)
                    Log.d(TAG, "Database instance received.")

                    passwordEntryDao = database.passwordEntryDao()
                    folderDao = database.folderDao()
                    securityManager = SecurityManager()
                    Log.d(TAG, "DAOs and SecurityManager are set up.")

                    initializationDeferred.complete(Unit)
                    Log.d(TAG, "Initialization successful. Deferred is completed.")
                } catch (e: Exception) {
                    Log.e(TAG, "Initialization failed", e)
                    initializationDeferred.completeExceptionally(e)
                }
            }
        }
    }

    /**
     * A new public function to allow other parts of the app to wait for initialization.
     */
    suspend fun awaitInitialization() {
        if (!initializationDeferred.isCompleted) {
            Log.d(TAG, "awaitInitialization: Waiting for repository to be ready...")
            initializationDeferred.await()
            Log.d(TAG, "awaitInitialization: Repository is now ready.")
        }
    }

    private suspend fun checkInitialized() {
        // This internal check now uses the new public awaiter function.
        awaitInitialization()
        Log.d(TAG, "checkInitialized: Already initialized.")
    }

    // --- (Rest of the file is unchanged) ---

    private fun decryptCredentials(entry: PasswordEntryWithCredentials): PasswordEntryWithCredentials {
        val decryptedCredentials = entry.credentials.map {
            it.copy(password = securityManager.decrypt(it.password))
        }
        return entry.copy(credentials = decryptedCredentials)
    }

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
