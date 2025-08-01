package com.myapplications.mypasswords.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * The Room Database class.
 */
@Database(entities = [Password::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}

/**
 * Singleton provider for AppDatabase with encrypted SQLCipher support.
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        // Retrieve passphrase using suspend function
        val passphrase = runBlocking {
            SecurityManager(context).getDatabasePassphrase().toByteArray()
        }

        // SQLCipher hook to configure encryption settings
        val hook = object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) {
                // Optional: log or monitor
            }

            override fun postKey(connection: SQLiteConnection) {
                // Apply encryption settings after key is set
                connection.execute("PRAGMA kdf_iter = 256000;", null, null)
                connection.execute("PRAGMA cipher_hmac_algorithm = HMAC_SHA512;", null, null)
                connection.execute("PRAGMA cipher_page_size = 4096;", null, null)
                connection.execute("PRAGMA cipher = 'aes-256-gcm';", null, null)
            }
        }

        // Create factory with passphrase, hook, and secure clear option
        val factory = SupportOpenHelperFactory(passphrase, hook, true)

        return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "mypasswords.db")
            .openHelperFactory(factory)
            .build()
    }
}