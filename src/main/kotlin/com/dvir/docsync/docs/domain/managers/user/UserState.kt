package com.dvir.docsync.docs.domain.managers.user

import com.dvir.docsync.core.model.ID

sealed interface UserState {
    data object InMain : UserState
    data class InDocument(val documentId: ID)
}