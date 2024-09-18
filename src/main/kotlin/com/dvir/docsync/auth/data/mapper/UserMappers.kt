package com.dvir.docsync.auth.data.mapper

import com.dvir.docsync.auth.data.entity.UserEntity
import com.dvir.docsync.core.model.User

fun User.toEntity(): UserEntity = UserEntity(
    username = username,
    password = password,
    salt = salt,
)

fun UserEntity.toUser(): User = User(
    username = username,
    password = password,
    salt = salt,
    id = id
)