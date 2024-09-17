package com.dvir.docsync.auth.data.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dvir.docsync.auth.domain.token.TokenClaim
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import java.util.*

class JwtTokenService: TokenService {
    override fun generate(config: TokenConfig, vararg claims: TokenClaim): String {
        var token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
        claims.forEach { claim ->
            token = token.withClaim(claim.name, claim.value)
        }

        return token.sign(Algorithm.HMAC256(config.secret))
    }
}