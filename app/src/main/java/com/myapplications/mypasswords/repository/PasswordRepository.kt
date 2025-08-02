package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.database.DatabaseProvider
import com.myapplications.mypasswords.database.FolderDao
import com.myapplications.mypasswords.database.PasswordDao
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

/**
 * Singleton repository for managing both Passwords and Folders.
 * Requires initialization before use, typically in the Application class.
 */
object PasswordRepository {

    private lateinit var passwordDao: PasswordDao
    private lateinit var folderDao: FolderDao

    /**
     * Initializes the repository with the application context.
     * This must be called once before any repository methods are used.
     */
    fun initialize(context: Context) {
        if (!::passwordDao.isInitialized) {
            val database = DatabaseProvider.getInstance(context)
            passwordDao = database.passwordDao()
            folderDao = database.folderDao()
        }
    }

    // --- Password Functions ---
    fun getRootPasswords(): Flow<List<Password>> {
        checkInitialized()
        return passwordDao.getRootPasswords()
    }

    fun getPasswordsInFolder(folderId: String): Flow<List<Password>> {
        checkInitialized()
        return passwordDao.getPasswordsInFolder(folderId)
    }

    suspend fun getPasswordById(id: String): Password? {
        checkInitialized()
        return passwordDao.getPasswordById(id)
    }

    suspend fun savePassword(password: Password) {
        checkInitialized()
        passwordDao.insertPassword(password)
    }

    suspend fun deletePassword(password: Password) {
        checkInitialized()
        passwordDao.deletePassword(password)
    }

    suspend fun updatePassword(password: Password) {
        checkInitialized()
        passwordDao.updatePassword(password)
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
     * Note: You will need to add the corresponding `deleteAll` queries to your DAOs.
     */
    suspend fun deleteAllData() {
        checkInitialized()
        // passwordDao.deleteAll() // Add @Query("DELETE FROM passwords") to PasswordDao
        // folderDao.deleteAll()   // Add @Query("DELETE FROM folders") to FolderDao
    }


    /**
     * Throws an exception if the repository has not been initialized.
     */
    private fun checkInitialized() {
        if (!::passwordDao.isInitialized) {
            throw IllegalStateException("PasswordRepository not initialized. Call initialize() in your Application class.")
        }
    }
}