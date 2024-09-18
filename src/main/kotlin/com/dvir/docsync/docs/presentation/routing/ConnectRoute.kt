package com.dvir.docsync.docs.presentation.routing

import com.dvir.docsync.auth.domain.repository.UserRepository
import com.dvir.docsync.core.model.Result
import com.dvir.docsync.core.user.OnlineUser
import com.dvir.docsync.core.user.UserManager
import com.dvir.docsync.core.user.UserState
import com.dvir.docsync.docs.domain.managers.documents.DocumentsManager
import com.dvir.docsync.docs.presentation.communication.requests.DocAction
import com.dvir.docsync.docs.presentation.communication.requests.DocListAction
import com.dvir.docsync.docs.presentation.communication.responses.DocListResponse
import com.dvir.docsync.docs.presentation.communication.responses.ErrorResponse
import com.dvir.docsync.docs.presentation.communication.send.sendResponse
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.connect(
    userRepository: UserRepository,
    documentsManager: DocumentsManager,
) {
    authenticate {
        webSocket("/connect") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                close(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "Authentication token required"
                    )
                )
                return@webSocket
            }

            val id = principal.payload.getClaim("id").asString()
            val user = userRepository.getUserById(id) ?: return@webSocket
            if (UserManager.isUserOnline(user.username)) {
                close(
                    CloseReason(
                        CloseReason.Codes.CANNOT_ACCEPT,
                        "User is already online"
                    )
                )
                return@webSocket
            }
            val onlineUser = OnlineUser(
                username = user.username,
                socket = this
            )
            UserManager.addUser(onlineUser)
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val requestBody = frame.readText()

                        val state = UserManager.getUser(user.username)!!.state
                        if (state is UserState.InMain) {
                            when (val request = Json.decodeFromString<DocListAction>(requestBody)) {
                                DocListAction.GetAllDocs -> {
                                    val docs = documentsManager.getAllDocs(onlineUser.username)
                                    sendResponse(DocListResponse.Docs(docs))
                                }
                                is DocListAction.GetDoc -> {
                                    val documentResult = documentsManager.addUserToDoc(
                                        onlineUser.username,
                                        request.docId
                                    )
                                    if (documentResult is Result.Success) {
                                        sendResponse(DocListResponse.Doc(documentResult.data!!))
                                    } else {
                                        sendResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                                is DocListAction.CreateDoc -> {
                                    val documentResult = documentsManager.createDoc(
                                        onlineUser.username,
                                        docName = request.docName
                                    )
                                    if (documentResult is Result.Success) {
                                        sendResponse(DocListResponse.Doc(documentResult.data!!))
                                    } else {
                                        sendResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                                is DocListAction.RemoveDoc -> {
                                    val documentResult = documentsManager.removeDoc(
                                        onlineUser.username,
                                        docId = request.docId
                                    )
                                    if (documentResult is Result.Success) {
                                        sendResponse(DocListResponse.Docs(documentResult.data!!))
                                    } else {
                                        sendResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                            }
                        } else if (state is UserState.InDocument) {
                            val request = Json.decodeFromString<DocAction>(requestBody)

                        }
                    }
                }
            } finally {
                documentsManager.removeUserFromDoc(onlineUser.username)
                UserManager.removeUser(onlineUser.username)
            }
        }
    }
}