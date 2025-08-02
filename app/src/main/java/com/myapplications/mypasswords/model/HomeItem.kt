// FILE: com/myapplications/mypasswords/model/HomeItem.kt
package com.myapplications.mypasswords.model

/**
 * A sealed interface to represent the different types of items that can appear on the home screen.
 * This allows them to be displayed in a single list.
 */
sealed interface HomeItem {
    val id: String // Common property for stable IDs in LazyColumn

    data class FolderItem(val folder: Folder) : HomeItem {
        override val id: String get() = folder.id
    }

    data class PasswordItem(val password: Password) : HomeItem {
        override val id: String get() = password.id
    }
}
