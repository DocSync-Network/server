package com.dvir.docsync.docs.domain.managers.cursor

data class CursorData(
    val start: CursorPosition,
    val end: CursorPosition? = null
)
