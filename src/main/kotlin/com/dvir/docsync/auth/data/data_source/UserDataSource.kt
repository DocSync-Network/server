package com.dvir.docsync.auth.data.data_source

import com.dvir.docsync.auth.data.entity.UserEntity
import com.dvir.docsync.core.model.ID
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserDataSource(
    db: CoroutineDatabase
) {
    private val users = db.getCollection<UserEntity>(collectionName = "users")

    suspend fun getUserByUsername(username: String): UserEntity? {
        return users.findOne(UserEntity::username eq username)
    }

    suspend fun getUserById(id: String): UserEntity? {
        return users.findOneById(UserEntity::id eq id)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return users.find().toList()
    }

    suspend fun insertUser(user: UserEntity): ID? {
        return if (users.insertOne(user).wasAcknowledged())
            return user.id else null
    }

    suspend fun updateUser(user: UserEntity): Boolean {
        return users.updateOneById(user.id, user).wasAcknowledged()
    }
}