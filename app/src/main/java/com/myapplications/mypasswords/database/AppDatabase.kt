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

    // The getInstance function now uses a Mutex for thread-safe access.
    suspend fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: mutex.withLock {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }
    }

    private suspend fun buildDatabase(context: Context): AppDatabase {
        val passphrase = SecurityManager().getDatabasePassphrase(context).toByteArray()

        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {}
            override fun postKey(connection: SQLiteConnection?) {}
        }

        val factory = SupportOpenHelperFactory(passphrase, hook, true)

        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mypasswords.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(false)
            .build()
    }
}
