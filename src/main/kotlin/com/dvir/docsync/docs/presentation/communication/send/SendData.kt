package com.dvir.docsync.docs.presentation.communication.send

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> WebSocketSession.sendResponse(response: T) {
    send(
        Frame.Text(
            Json.encodeToString(response)
        )
    )
}

