package com.dvir.docsync.auth.presentation.routing

import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureAuthRouting(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        login(userRepository, hashingService, tokenService, tokenConfig)
        signup(userRepository, hashingService, tokenService, tokenConfig)
        validate()
    }
}