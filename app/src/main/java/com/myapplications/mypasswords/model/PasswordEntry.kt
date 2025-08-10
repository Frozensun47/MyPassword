// FILE: com/myapplications/mypasswords/model/PasswordEntry.kt
package com.myapplications.mypasswords.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

// This is the main entry, which just holds the title and folder info.
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val folderId: String? = null
)

// This holds the actual username/password pairs.
@Entity(
    tableName = "credentials",
    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE // If the entry is deleted, all its credentials are deleted too.
        )
    ],
    indices = [Index("entryId")]
)
data class Credential(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entryId: String,
    val username: String,
    val password: String // This will be encrypted by the repository
)

// This class combines the entry and its list of credentials for easy use in the ViewModel and UI.
data class PasswordEntryWithCredentials(
    @Embedded val entry: PasswordEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val credentials: List<Credential>
)
