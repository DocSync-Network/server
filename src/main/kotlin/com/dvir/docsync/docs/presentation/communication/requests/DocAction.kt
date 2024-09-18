package com.dvir.docsync.docs.presentation.communication.requests

import com.dvir.docsync.docs.domain.model.Character
import com.dvir.docsync.docs.domain.model.CharacterConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DocAction {
    @Serializable
    @SerialName("edit")
    data class Edit(val config: CharacterConfig): DocAction()
    @Serializable
    @SerialName("add")
    data class Add(val char: Character) : DocAction()
    @Serializable
    @SerialName("cursor")
    data class UpdateCursor(val pos: Int) : DocAction()
    @Serializable
    @SerialName("selection")
    data class UpdateSelection(
        val start: Int,
        val end: Int
    ) : DocAction()
    @Serializable
    @SerialName("remove")
    data object Remove : DocAction()
}