package com.dvir.docsync.docs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterConfig(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val color: String = "#000000"
)
