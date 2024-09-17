package com.dvir.docsync.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val docs: List<ID> = emptyList(),
)
