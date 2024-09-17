package com.dvir.docsync.auth.data.mapper

import com.dvir.docsync.auth.data.entity.UserEntity
import com.dvir.docsync.core.model.User
import org.bson.types.ObjectId

fun User.toEntity(salt: String, password: String): UserEntity = UserEntity(
    username = username,
    password = password,
    salt = salt,
    data = data,
)

fun User.toEntity(): UserEntity = UserEntity(
    username = username,
    password = password,
    salt = salt,
    data = data,
)

fun UserEntity.toUser(): User = User(
    username = username,
    password = password,
    salt = salt,
    data = data,
    id = id
)