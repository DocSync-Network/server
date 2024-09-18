package com.dvir.docsync.docs.domain.managers.user

import io.ktor.websocket.*

data class OnlineUser(
    val username: String,
    val state: UserState = UserState.InMain,
    val socket: WebSocketSession
)
