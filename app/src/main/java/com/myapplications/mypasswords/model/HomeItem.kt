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

    // This is the corrected version. It now wraps a PasswordEntryWithCredentials object.
    data class PasswordEntryItem(val entryWithCredentials: PasswordEntryWithCredentials) : HomeItem {
        override val id: String get() = entryWithCredentials.entry.id
    }
}
