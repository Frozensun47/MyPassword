// FILE: com/myapplications/mypasswords/database/AppDatabase.kt
package com.myapplications.mypasswords.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myapplications.mypasswords.model.Credential
import com.myapplications.mypasswords.model.Folder
import com.myapplications.mypasswords.model.PasswordEntry
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import android.util.Log

/**
 * The Room Database class for the application.
 * It now includes both Password and Folder entities.
 */
@Database(entities = [PasswordEntry::class, Credential::class, Folder::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordEntryDao(): PasswordEntryDao
    abstract fun folderDao(): FolderDao
}

/**
 * A singleton object to provide the AppDatabase instance.
 * This pattern ensures the encrypted database is created only once.
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null
    private val mutex = Mutex()
    private const val TAG = "DatabaseProvider"

    // The getInstance function is now truly non-blocking.
    suspend fun getInstance(context: Context): AppDatabase {
        Log.d(TAG, "getInstance() called. Checking for existing instance.")
        // First check without a lock for fast path access.
        val currentInstance = INSTANCE
        if (currentInstance != null) {
            Log.d(TAG, "Instance already exists. Returning.")
            return currentInstance
        }

        // Fetch passphrase outside the lock, as this is a suspend function.
        Log.d(TAG, "Instance does not exist. Fetching passphrase.")
        val passphrase = SecurityManager().getDatabasePassphrase(context).toByteArray()
        Log.d(TAG, "Passphrase fetched. Acquiring mutex lock.")

        return mutex.withLock {
            Log.d(TAG, "Mutex lock acquired. Second check for instance.")
            // Second check inside the lock, as another coroutine may have initialized the database.
            INSTANCE ?: buildDatabase(context, passphrase).also {
                INSTANCE = it
                Log.d(TAG, "Database instance built and assigned. Releasing mutex lock.")
            }
        }
    }

    private suspend fun buildDatabase(context: Context, passphrase: ByteArray): AppDatabase {
        Log.d(TAG, "Building database...")
        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {}
            override fun postKey(connection: SQLiteConnection?) {}
        }

        val factory = SupportOpenHelperFactory(passphrase, hook, true)

        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mypasswords.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(false)
            .build()

        Log.d(TAG, "Database build complete.")
        return db
    }
}
