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
import com.dvir.docsync.docs.presentation.communication.send.sendDocListResponse
import com.dvir.docsync.docs.presentation.communication.send.sendErrorResponse
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
            var onlineUser = OnlineUser(
                username = user.username,
                socket = this
            )
            UserManager.addUser(onlineUser)
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val requestBody = frame.readText()
                        val state = UserManager.getUser(user.username)!!.state
                        onlineUser = onlineUser.copy(state = state)
                        if (state is UserState.InMain) {
                            when (val request = Json.decodeFromString<DocListAction>(requestBody)) {
                                DocListAction.GetAllDocs -> {
                                    val docs = documentsManager.getAllDocs(onlineUser.username)
                                    sendDocListResponse(DocListResponse.Docs(docs))
                                }
                                is DocListAction.GetDoc -> {
                                    val documentResult = documentsManager.addUserToDoc(
                                        onlineUser,
                                        request.docId
                                    )
                                    if (documentResult is Result.Success) {
                                        sendDocListResponse(DocListResponse.Doc(documentResult.data!!))
                                    } else {
                                        sendErrorResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                                is DocListAction.CreateDoc -> {
                                    val documentResult = documentsManager.createDoc(
                                        onlineUser.username,
                                        docName = request.docName
                                    )
                                    if (documentResult is Result.Success) {
                                        sendDocListResponse(DocListResponse.Doc(documentResult.data!!))
                                    } else {
                                        sendErrorResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                                is DocListAction.RemoveDoc -> {
                                    val documentResult = documentsManager.removeDoc(
                                        onlineUser.username,
                                        docId = request.docId
                                    )
                                    if (documentResult is Result.Success) {
                                        sendDocListResponse(DocListResponse.Docs(documentResult.data!!))
                                    } else {
                                        sendErrorResponse(ErrorResponse(documentResult.message!!))
                                    }
                                }
                            }
                        } else if (state is UserState.InDocument) {
                            when (val request = Json.decodeFromString<DocAction>(requestBody)) {
                                is DocAction.Add -> documentsManager.addCharacter(
                                        user = onlineUser,
                                        character = request.char
                                    )
                                is DocAction.AddAccess -> documentsManager.addAccess(
                                    user = onlineUser,
                                    addedUsername = request.addedUsername
                                )
                                is DocAction.RemoveAccess -> documentsManager.removeAccess(
                                    user = onlineUser,
                                    removedUsername = request.removedUsername
                                )
                                is DocAction.Edit -> documentsManager.editCharacters(
                                    user = onlineUser,
                                    config = request.config
                                )
                                is DocAction.UpdateCursorData -> documentsManager.updateCursor(
                                    user = onlineUser,
                                    cursorData = request.data
                                )
                                DocAction.Remove -> {
                                    documentsManager.removeCharacter(onlineUser)
                                }
                                DocAction.Save -> {
                                    documentsManager.saveDocument(state.documentId)
                                }
                                DocAction.LeaveDoc -> {
                                    documentsManager.removeUserFromDoc(onlineUser.username)
                                    UserManager.changeUserState(onlineUser.username, UserState.InMain)
                                }
                            }
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