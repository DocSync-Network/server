package com.dvir.docsync.docs.domain.managers.cursor

interface CursorAction {
    data object Add: CursorAction
    data object Remove: CursorAction
}