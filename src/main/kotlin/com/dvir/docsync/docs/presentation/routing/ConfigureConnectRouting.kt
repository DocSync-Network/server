package com.dvir.docsync.docs.presentation.routing

import com.dvir.docsync.ServiceLocator
import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.docs.domain.managers.documents.DocumentsManager
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureConnectRouting() {
    routing {
        connect(
            userRepository = ServiceLocator.get<UserRepository>(),
            documentsManager = ServiceLocator.get<DocumentsManager>()
        )
    }
}