package com.dvir.docsync.auth

import com.dvir.docsync.ServiceLocator
import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import com.dvir.docsync.auth.presentation.routing.configureAuthRouting
import io.ktor.server.application.*

fun Application.authModule() {
    configureAuthRouting(
        ServiceLocator.get<UserRepository>(),
        ServiceLocator.get<HashingService>(),
        ServiceLocator.get<TokenService>(),
        ServiceLocator.get<TokenConfig>(),
    )
}