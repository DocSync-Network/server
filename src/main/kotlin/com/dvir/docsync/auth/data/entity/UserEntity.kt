package com.dvir.docsync.auth.data.entity

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.core.model.UserData
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UserEntity(
    val username: String,
    val password: String,
    val salt: String,

    val data: UserData,

    @BsonId
    val id: ID = ObjectId().toString()
)