package com.dvir.docsync.docs.presentation.communication.send

import com.dvir.docsync.docs.presentation.communication.responses.DocActionResponse
import com.dvir.docsync.docs.presentation.communication.responses.DocListResponse
import com.dvir.docsync.docs.presentation.communication.responses.ErrorResponse
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun WebSocketSession.sendDocListResponse(response: DocListResponse) {
    send(Frame.Text(Json.encodeToString(response)))
}

suspend fun WebSocketSession.sendDocActionResponse(response: DocActionResponse) {
    send(Frame.Text(Json.encodeToString(response)))
}

suspend fun WebSocketSession.sendErrorResponse(response: ErrorResponse) {
    send(Frame.Text(Json.encodeToString(response)))
}