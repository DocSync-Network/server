package com.dvir.docsync.docs.domain.managers.user

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.managers.cursor.CursorData

sealed interface UserState {
    data object InMain : UserState
    data class InDocument(
        val documentId: ID,
        val cursorData: CursorData
    ): UserState
}