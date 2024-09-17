package com.dvir.docsync.auth.domain.security

data class SaltedHash(
    val hash: String,
    val salt: String
)
