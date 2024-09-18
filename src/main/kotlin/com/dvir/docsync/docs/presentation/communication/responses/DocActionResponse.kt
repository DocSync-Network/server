package com.dvir.docsync.docs.presentation.communication.responses

import com.dvir.docsync.docs.domain.model.Character
import com.dvir.docsync.docs.domain.model.CharacterConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DocActionResponse {
    @Serializable
    @SerialName("added")
    data class Added(val username: String, val char: Character) : DocActionResponse()
    @Serializable
    @SerialName("removed")
    data class Remove(val username: String) : DocActionResponse()
    @Serializable
    @SerialName("edited")
    data class Edited(val username: String, val config: CharacterConfig) : DocActionResponse()
    @Serializable
    @SerialName("updatedCursor")
    data class UpdatedCursor(val username: String, val pos: Int) : DocActionResponse()
    @Serializable
    @SerialName("updatedSelection")
    data class UpdatedSelection(val username: String, val start: Int, val end: Int) : DocActionResponse()
}