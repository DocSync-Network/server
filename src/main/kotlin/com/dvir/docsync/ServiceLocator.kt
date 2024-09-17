package com.dvir.docsync

import com.dvir.docsync.auth.data.data_source.UserDataSource
import com.dvir.docsync.auth.data.repository.UserRepositoryImpl
import com.dvir.docsync.auth.data.security.SHA256HashingService
import com.dvir.docsync.auth.data.token.JwtTokenService
import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.token.TokenConfig
import com.dvir.docsync.auth.domain.token.TokenService
import com.dvir.docsync.docs.data.DocsDataSource
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object ServiceLocator {
    val services = mutableMapOf<Class<*>, Any>()

    private inline fun <reified T : Any> register(service: T) {
        services[T::class.java] = service
    }

    inline fun <reified T : Any> get(): T {
        return services[T::class.java] as? T
            ?: throw IllegalStateException("Service ${T::class.java.simpleName} not found: \n1) check the setup function\n2) check if the setup function called during setup")
    }

    fun setup() {
        val dbName = "ktor-docsync"
        val database = KMongo.createClient(
            connectionString = "mongodb://localhost:27017"
        ).coroutine.getDatabase(dbName)

        val userDataSource = UserDataSource(database)
        val docsDataSource = DocsDataSource(database)
        val hashingService = SHA256HashingService()
        val userRepository = UserRepositoryImpl(userDataSource)
        val tokenService = JwtTokenService()
        val tokenConfig = TokenConfig(
            issuer = "http://0.0.0.0:8080",
            audience = "users",
            expiresIn = 2629746000L, // 1 month
            secret = System.getenv("JWT_SECRET") ?: "default_secret"
        )

        register<UserDataSource>(userDataSource)
        register<DocsDataSource>(docsDataSource)
        register<HashingService>(hashingService)
        register<UserRepository>(userRepository)
        register<TokenService>(tokenService)
        register<TokenConfig>(tokenConfig)
    }
}

