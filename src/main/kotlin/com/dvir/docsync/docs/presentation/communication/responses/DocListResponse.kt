package com.dvir.docsync.docs.presentation.communication.responses

import com.dvir.docsync.docs.domain.model.Document
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DocListResponse {
    @Serializable
    @SerialName("doc")
    data class Doc(val doc: Document) : DocListResponse()
    @Serializable
    @SerialName("docs")
    data class Docs(val docs: List<Document>) : DocListResponse()
}