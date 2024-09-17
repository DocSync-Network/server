package com.dvir.docsync.docs.domain.managers.user

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.managers.cursor.CursorData
import io.ktor.websocket.*

data class OnlineUser(
    val username: String,
    val state: UserState = UserState.InMain,
    val documentId: ID,
    val cursorData: CursorData?,
    val socket: WebSocketSession
)
