package com.dvir.docsync.docs.presentation.communication.requests

import com.dvir.docsync.core.model.ID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DocListAction {
    @Serializable
    @SerialName("getDoc")
    data class GetDoc(val id: ID) : DocListAction
    @Serializable
    @SerialName("getAllDocs")
    data object GetAllDocs : DocListAction
}