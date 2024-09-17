package com.dvir.docsync.auth.presentation.communication.send

import com.dvir.docsync.auth.domain.token.TokenClaim
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import com.dvir.docsync.auth.presentation.communication.responses.AuthResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun RoutingCall.sendResponse(response: AuthResponse, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(status, Json.encodeToString(response))
}

suspend fun RoutingCall.sendError(message: String, status: HttpStatusCode = HttpStatusCode.BadRequest) {
    sendResponse(AuthResponse.ErrorResponse(message), status)
}

suspend fun RoutingCall.generateAndSendToken(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    vararg claims: TokenClaim
) {
    val token = tokenService.generate(
        config = tokenConfig,
        claims = claims
    )

    sendResponse(
        AuthResponse.AuthCompleted(token)
    )
}

