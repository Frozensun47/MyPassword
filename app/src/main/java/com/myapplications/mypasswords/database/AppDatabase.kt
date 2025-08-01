package com.myapplications.mypasswords.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * The Room Database class. It no longer manages its own instance directly
 * to better handle asynchronous initialization.
 */
@Database(entities = [Password::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}

/**
 * A singleton object to provide the AppDatabase instance.
 * This pattern ensures the database is created only once and handles
 * the async retrieval of the encryption passphrase.
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        // Use double-checked locking to ensure the instance is created only once.
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        // Since getDatabasePassphrase is a suspend function, we need a coroutine.
        // runBlocking is acceptable here because database creation is a one-time,
        // blocking operation that must complete for the app to function.
        val passphrase = runBlocking {
            SecurityManager(context).getDatabasePassphrase().toByteArray()
        }

        // Define the hook to configure the database after the key is set.
        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {}
            override fun postKey(connection: SQLiteConnection?) {
            }
        }

        // The factory constructor is updated to include the 'clearPassphrase' boolean argument.
        // Setting this to 'true' is recommended for security.
        val factory = SupportOpenHelperFactory(passphrase, hook, true)

        // Build the Room database instance with the encryption factory
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mypasswords.db"
        )
            .openHelperFactory(factory)
            .build()
    }
}
