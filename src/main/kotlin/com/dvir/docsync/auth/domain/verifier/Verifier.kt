package com.dvir.docsync.auth.domain.verifier

import com.dvir.docsync.core.constants.Constants
import io.ktor.http.*
import io.ktor.server.response.*

class Verifier {
    companion object {
        fun verifyUsername(username: String): VerifierResult {
            if (username.length < Constants.MIN_USERNAME_LENGTH) {
                return VerifierResult(
                    isValid = false,
                    message = "Username must be at least ${Constants.MIN_USERNAME_LENGTH} characters"
                )
            }
            if (username.length > Constants.MAX_USERNAME_LENGTH) {
                return VerifierResult(
                    isValid = false,
                    message = "Username must be at most ${Constants.MAX_USERNAME_LENGTH} characters"
                )
            }
            return VerifierResult(isValid = true)
        }
        fun verifyPassword(password: String): VerifierResult {
            val hasUpperCase = password.any {
                it.isUpperCase()
            }
            val hasLowerCase = password.any {
                it.isLowerCase()
            }
            val hasDigit = password.any {
                it.isDigit()
            }
            val hasSpecialChar = password.any {
                it.isLetterOrDigit().not()
            }
            val requirements = listOf(
                "at least ${Constants.MIN_PASSWORD_LENGTH} characters" to (password.length >= Constants.MIN_PASSWORD_LENGTH),
                "an uppercase letter" to hasUpperCase,
                "a lowercase letter" to hasLowerCase,
                "a digit" to hasDigit,
                "a special character" to hasSpecialChar
            )
            val missingRequirements = requirements.filterNot { it.second }.map { it.first }

            return if (missingRequirements.isEmpty()) {
                VerifierResult(isValid = true)
            } else {
                if(missingRequirements.size == 1) {
                    VerifierResult(
                        isValid = false,
                        message = "Your password also need to contain ${missingRequirements.lastOrNull() ?: ""}"
                    )
                } else {
                    VerifierResult(
                        isValid = false,
                        message = "Your password also need to contain ${missingRequirements.dropLast(1).joinToString(", ")}, and ${missingRequirements.lastOrNull() ?: ""}."
                    )
                }
            }
        }
    }
}