package com.dvir.docsync.docs.presentation.communication.responses

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.model.Document
import com.dvir.docsync.docs.presentation.communication.requests.DocListAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DocListResponse {
    @Serializable
    @SerialName("doc")
    data class Doc(val doc: Document) : DocListResponse
    @Serializable
    @SerialName("Docs")
    data class Docs(val docs: List<Document>) : DocListResponse
}