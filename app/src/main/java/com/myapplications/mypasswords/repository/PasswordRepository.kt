// FILE: com/myapplications/mypasswords/repository/PasswordRepository.kt
package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.database.DatabaseProvider
import com.myapplications.mypasswords.database.FolderDao
import com.myapplications.mypasswords.database.PasswordDao
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PasswordRepository {

    private lateinit var passwordDao: PasswordDao
    private lateinit var folderDao: FolderDao
    private lateinit var securityManager: SecurityManager

    /**
     * Initializes the repository with the application context.
     * This must be called once before any repository methods are used.
     */
    fun initialize(context: Context) {
        if (!::passwordDao.isInitialized) {
            // **THE FIX IS HERE**:
            // To prevent potential memory leaks, we explicitly use the application context,
            // which is safe to store in a long-lived singleton object like this repository.
            val appContext = context.applicationContext

            val database = DatabaseProvider.getInstance(appContext)
            passwordDao = database.passwordDao()
            folderDao = database.folderDao()
            securityManager = SecurityManager()
        }
    }

    // --- Password Functions (with Encryption/Decryption) ---

    fun getRootPasswords(): Flow<List<Password>> {
        checkInitialized()
        return passwordDao.getRootPasswords().map { list ->
            list.map { it.copy(password = securityManager.decrypt(it.password)) }
        }
    }

    fun getPasswordsInFolder(folderId: String): Flow<List<Password>> {
        checkInitialized()
        return passwordDao.getPasswordsInFolder(folderId).map { list ->
            list.map { it.copy(password = securityManager.decrypt(it.password)) }
        }
    }

    suspend fun getPasswordById(id: String): Password? {
        checkInitialized()
        val encryptedPassword = passwordDao.getPasswordById(id)
        return encryptedPassword?.copy(password = securityManager.decrypt(encryptedPassword.password))
    }

    suspend fun savePassword(password: Password) {
        checkInitialized()
        val encryptedPassword = password.copy(password = securityManager.encrypt(password.password))
        passwordDao.insertPassword(encryptedPassword)
    }

    suspend fun deletePassword(password: Password) {
        checkInitialized()
        // No need to encrypt/decrypt for deletion
        passwordDao.deletePassword(password)
    }

    suspend fun updatePassword(password: Password) {
        checkInitialized()
        val encryptedPassword = password.copy(password = securityManager.encrypt(password.password))
        passwordDao.updatePassword(encryptedPassword)
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

    /**
     * Deletes all data from the database. Use with caution.
     */
    suspend fun deleteAllData() {
        checkInitialized()
        // You will need to add these methods to your DAOs
         passwordDao.deleteAll()
         folderDao.deleteAll()
    }

    private fun checkInitialized() {
        if (!::passwordDao.isInitialized) {
            throw IllegalStateException("PasswordRepository not initialized. Call initialize() in your Application class.")
        }
    }
}
