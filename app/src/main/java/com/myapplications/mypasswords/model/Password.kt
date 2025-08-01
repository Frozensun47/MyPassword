package com.myapplications.mypasswords.model

data class Password(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val folder: String? = null,
    val colorHex: String? = null
)