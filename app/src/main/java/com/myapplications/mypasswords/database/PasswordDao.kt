// FILE: com/myapplications/mypasswords/database/PasswordEntryDao.kt
package com.myapplications.mypasswords.database

import androidx.room.*
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.model.PasswordEntryWithCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordEntryDao {
    // --- Password Entry Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PasswordEntry)

    @Update
    suspend fun updateEntry(entry: PasswordEntry)

    @Delete
    suspend fun deleteEntry(entry: PasswordEntry)

    // --- Credential Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: List<Credential>)

    @Query("DELETE FROM credentials WHERE entryId = :entryId")
    suspend fun deleteCredentialsForEntry(entryId: String)

    // --- Combined Queries ---
    @Transaction
    @Query("SELECT * FROM password_entries WHERE id = :entryId")
    fun getEntryWithCredentials(entryId: String): Flow<PasswordEntryWithCredentials?>

    @Transaction
    @Query("SELECT * FROM password_entries WHERE folderId IS NULL")
    fun getRootEntriesWithCredentials(): Flow<List<PasswordEntryWithCredentials>>

    @Transaction
    @Query("SELECT * FROM password_entries WHERE folderId = :folderId")
    fun getEntriesWithCredentialsInFolder(folderId: String): Flow<List<PasswordEntryWithCredentials>>

    @Query("DELETE FROM password_entries")
    suspend fun deleteAll()
    // Helper to save an entry and its credentials in one transaction
    @Transaction
    suspend fun saveEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) {
        insertEntry(entry)
        // Delete old credentials before inserting new ones to handle updates/deletions
        deleteCredentialsForEntry(entry.id)
        insertCredentials(credentials)
    }
}
