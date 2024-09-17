package com.dvir.docsync.plugins

import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import com.dvir.docsync.auth.presentation.routing.login
import com.dvir.docsync.auth.presentation.routing.signup
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureRouting() {
    routing {
        webSocket("/connect") {

        }
    }
}