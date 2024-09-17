package com.dvir.docsync.auth.domain.token

data class TokenClaim(
    val name: String,
    val value: String
)