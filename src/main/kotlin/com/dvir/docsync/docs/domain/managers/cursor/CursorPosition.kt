package com.dvir.docsync.docs.domain.managers.cursor

import kotlinx.serialization.Serializable

@Serializable
data class CursorPosition(
    val line: Int,
    val column: Int
)
