package com.dvir.docsync.docs.presentation.communication.requests

import com.dvir.docsync.core.model.ID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DocListAction {
    @Serializable
    @SerialName("getDoc")
    data class GetDoc(val docId: ID) : DocListAction()
    @Serializable
    @SerialName("createDoc")
    data class CreateDoc(val docName: String) : DocListAction()
    @Serializable
    @SerialName("removeDoc")
    data class RemoveDoc(val docId: ID) : DocListAction()
    @Serializable
    @SerialName("getAllDocs")
    data object GetAllDocs : DocListAction()
}