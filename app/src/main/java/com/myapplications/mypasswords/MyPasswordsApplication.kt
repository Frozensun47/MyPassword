// FILE: com/myapplications/mypasswords/MyPasswordsApplication.kt
package com.myapplications.mypasswords

import android.app.Application
import com.myapplications.mypasswords.repository.PasswordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPasswordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load native library and initialize repository asynchronously.
        System.loadLibrary("sqlcipher")

        // Use a coroutine to call the suspend function off the main thread.
        CoroutineScope(Dispatchers.IO).launch {
            PasswordRepository.initialize(applicationContext)
        }
    }
}
