package com.dvir.docsync.auth.domain.verifier

data class VerifierResult(
    val isValid: Boolean,
    val message: String? = null,
)
