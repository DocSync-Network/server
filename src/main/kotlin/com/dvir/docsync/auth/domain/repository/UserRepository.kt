package com.dvir.docsync.auth.domain.repository

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.core.model.User

interface UserRepository {
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(id: String): User?
    suspend fun getAllUsers(): List<User>

    suspend fun addUser(user: User): ID?
    suspend fun updateUser(user: User): Boolean
}