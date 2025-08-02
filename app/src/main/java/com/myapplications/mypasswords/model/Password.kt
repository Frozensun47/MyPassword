package com.myapplications.mypasswords.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "passwords",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId")]
)
data class Password(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val username: String,
    val password: String,
    val folderId: String? = null
)