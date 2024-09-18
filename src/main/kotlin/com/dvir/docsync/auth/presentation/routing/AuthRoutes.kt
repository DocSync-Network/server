package com.dvir.docsync.auth.presentation.routing

import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.security.SaltedHash
import com.dvir.docsync.auth.domain.token.TokenClaim
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import com.dvir.docsync.auth.domain.verifier.Verifier
import com.dvir.docsync.auth.presentation.communication.responses.AuthResponse
import com.dvir.docsync.auth.presentation.communication.send.generateAndSendToken
import com.dvir.docsync.auth.presentation.communication.send.sendError
import com.dvir.docsync.auth.presentation.communication.send.sendResponse
import com.dvir.docsync.core.model.User
import com.dvir.docsync.auth.presentation.communication.requests.AuthRequest
import com.dvir.docsync.core.user.UserManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.login(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("/login") {
        val request = call.receiveNullable<AuthRequest.Auth>() ?: kotlin.run {
            call.sendError("Invalid request")
            return@post
        }

        val user = userRepository.getUserByUsername(request.username)
        if (user == null) {
            call.sendError(
                "Invalid username or password",
                HttpStatusCode.Unauthorized
            )
            return@post
        }

        val isPasswordValid = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isPasswordValid) {
            call.sendError(
                "Invalid username or password",
                HttpStatusCode.Unauthorized
            )
            return@post
        }
        if (UserManager.isUserOnline(request.username)) {
            call.sendError(
                "User is already online",
                HttpStatusCode.Conflict
            )
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(name = "id", value = user.id!!)
        )

        call.sendResponse(
            AuthResponse.AuthCompleted(token)
        )
    }
}

fun Route.signup(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("/signup") {
        val request = call.receiveNullable<AuthRequest.Auth>() ?: kotlin.run {
            call.sendError("Invalid request")
            return@post
        }

        val usernameResult = Verifier.verifyUsername(request.username)
        val passwordResult = Verifier.verifyPassword(request.password)
        if (!usernameResult.isValid) {
            call.sendError(usernameResult.message!!)
            return@post
        }
        if (!passwordResult.isValid) {
            call.sendError(passwordResult.message!!)
            return@post
        }

        val isUserExists = userRepository.getUserByUsername(request.username) != null
        if (isUserExists) {
            call.sendError("This username is already taken", HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
        )

        val userId = userRepository.addUser(user)
        if (userId == null) {
            call.sendError("Unknown error occurred, please try again later", HttpStatusCode.InternalServerError)
            return@post
        }

        call.generateAndSendToken(
            tokenService = tokenService,
            tokenConfig = tokenConfig,
            TokenClaim(name = "id", value = userId)
        )
    }
}
