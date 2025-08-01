package com.myapplications.mypasswords.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey val id: String,
    val title: String,
    val username: String,
    val password: String,
    val folder: String? = null,
    val colorHex: String? = null
)