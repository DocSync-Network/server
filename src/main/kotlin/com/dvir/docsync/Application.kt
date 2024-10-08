package com.dvir.docsync

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.dvir.docsync.auth.authModule
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.docs.presentation.routing.configureConnectRouting
import com.dvir.docsync.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import org.slf4j.LoggerFactory

fun main() {
    ServiceLocator.setup()
    (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger("org.mongodb.driver").level = Level.ERROR

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets)
    configureSerialization()
    configureMonitoring()
    configureSecurity(ServiceLocator.get<TokenConfig>())
    configureConnectRouting()
    authModule()
}