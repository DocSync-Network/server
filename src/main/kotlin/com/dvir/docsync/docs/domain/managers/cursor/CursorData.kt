package com.dvir.docsync.docs.domain.managers.cursor

import kotlinx.serialization.Serializable

@Serializable
data class CursorData(
    val start: CursorPosition,
    val end: CursorPosition? = null
)
