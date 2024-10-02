package com.dvir.docsync.docs.presentation.communication.requests

import com.dvir.docsync.docs.domain.managers.cursor.CursorData
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
    @SerialName("addAccess")
    data class AddAccess(val addedUsername: String) : DocAction()
    @Serializable
    @SerialName("removeAccess")
    data class RemoveAccess(val removedUsername: String) : DocAction()
    @Serializable
    @SerialName("cursor")
    data class UpdateCursorData(val data: CursorData) : DocAction()
    @Serializable
    @SerialName("remove")
    data object Remove : DocAction()
    @Serializable
    @SerialName("save")
    data object Save : DocAction()
    @Serializable
    @SerialName("leaveDoc")
    data object LeaveDoc : DocAction()
}