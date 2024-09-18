package com.dvir.docsync.core.model

typealias ID = String

data class User(
    val username: String,
    val password: String,
    val salt: String,
    val id: ID? = null
)