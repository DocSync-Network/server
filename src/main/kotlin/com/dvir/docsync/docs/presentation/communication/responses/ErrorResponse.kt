package com.dvir.docsync.docs.presentation.communication.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("error")
data class ErrorResponse(
    val message: String
)
