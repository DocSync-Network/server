package com.dvir.docsync.docs.presentation.routing

import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Route.connect() {
    webSocket("/connect") {

    }
}