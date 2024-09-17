package com.dvir.docsync.auth.data.repository

import com.dvir.docsync.auth.data.data_source.UserDataSource
import com.dvir.docsync.auth.data.mapper.toEntity
import com.dvir.docsync.auth.data.mapper.toUser
import com.dvir.docsync.core.model.User
import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.core.model.ID

class UserRepositoryImpl(
    private val userDataSource: UserDataSource,
): UserRepository {
    override suspend fun getUserByUsername(username: String): User? {
        return userDataSource.getUserByUsername(username)?.toUser()
    }

    override suspend fun getUserById(id: String): User? {
        return userDataSource.getUserById(id)?.toUser()
    }

    override suspend fun getAllUsers(): List<User> {
        return userDataSource.getAllUsers().map { entity ->
            entity.toUser()
        }
    }

    override suspend fun addUser(user: User): ID? {
        return userDataSource.insertUser(
            user = user.toEntity()
        )
    }

    override suspend fun updateUser(user: User): Boolean {
        return userDataSource.updateUser(user.toEntity())
    }
}