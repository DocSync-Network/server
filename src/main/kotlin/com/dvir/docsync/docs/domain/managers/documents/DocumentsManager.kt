package com.dvir.docsync.docs.domain.managers.documents

import com.dvir.docsync.core.user.UserManager
import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.data.DocsDataSource
import com.dvir.docsync.docs.domain.managers.cursor.CursorManager
import com.dvir.docsync.core.user.UserState
import com.dvir.docsync.docs.domain.model.Document
import com.dvir.docsync.core.model.Result
import com.dvir.docsync.core.user.OnlineUser
import com.dvir.docsync.docs.domain.managers.cursor.CursorData
import com.dvir.docsync.docs.domain.managers.cursor.CursorPosition
import com.dvir.docsync.docs.domain.model.Character
import com.dvir.docsync.docs.domain.model.CharacterConfig
import com.dvir.docsync.docs.presentation.communication.responses.DocActionResponse
import com.dvir.docsync.docs.presentation.communication.responses.DocListResponse
import com.dvir.docsync.docs.presentation.communication.send.sendResponse
import java.util.concurrent.ConcurrentHashMap

class DocumentsManager(
    private val docsDataSource: DocsDataSource
) {
    private val onlineDocuments = ConcurrentHashMap<ID, DocumentManager>()

    suspend fun addAccess(user: OnlineUser, addedUsername: String) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.addAccess(user.username, addedUsername)

        val addedUser = UserManager.getUser(addedUsername) ?: return
        addedUser.socket.sendResponse(
            DocListResponse.Docs(getAllDocs(addedUsername))
        )
    }

    suspend fun removeAccess(user: OnlineUser, removedUsername: String) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.removeAccess(user.username, removedUsername)

        val addedUser = UserManager.getUser(removedUsername) ?: return
        if (addedUser.state !is UserState.InDocument)
            return
        if (addedUser.state.documentId != documentManager.getDocument().id)
            return

        addedUser.socket.sendResponse(
            DocActionResponse.AccessRemoved
        )
        UserManager.changeUserState(removedUsername, UserState.InMain)
    }

    suspend fun updateCursor(user: OnlineUser, cursorData: CursorData) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.updateCursor(user.username, cursorData)
        documentManager.activeUsers.forEach { (_, user) ->
            user.socket.sendResponse(DocActionResponse.UpdatedCursorData(user.username, cursorData))
        }
    }

    suspend fun addCharacter(user: OnlineUser, character: Character) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.addCharacter(character, user.username)
        documentManager.activeUsers.forEach { (_, user) ->
            user.socket.sendResponse(DocActionResponse.Added(user.username, character))
        }
    }

    suspend fun removeCharacter(user: OnlineUser) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.removeCharacters(user.username)
        documentManager.activeUsers.forEach { (_, user) ->
            user.socket.sendResponse(DocActionResponse.Remove(user.username))
        }
    }

    suspend fun editCharacters(user: OnlineUser, config: CharacterConfig) {
        if (user.state !is UserState.InDocument)
            return

        val documentManager = onlineDocuments[user.state.documentId] ?: return
        documentManager.editCharacters(user.username, config)
        documentManager.activeUsers.forEach { (_, user) ->
            user.socket.sendResponse(DocActionResponse.Edited(user.username, config))
        }
    }

    suspend fun createDoc(username: String, docName: String): Result<Document> {
        val document = Document(
            owner = username,
            name = docName,
            creationDate = System.currentTimeMillis(),
            access = mutableListOf(username),
            content = mutableListOf()
        )
        val wasAcknowledged = docsDataSource.insertDoc(document)
        return if (wasAcknowledged) {
            addUserToDoc(UserManager.getUser(username)!!, document.id)
        } else {
            Result.Error("Could not create document")
        }
    }

    suspend fun removeDoc(username: String, docId: ID): Result<List<Document>> {
        val document = docsDataSource.getDocById(docId) ?: return Result.Error("Document not found")
        if (document.owner != username) {
            return Result.Error("You are not allowed to delete this document")
        }

        val wasAcknowledged = docsDataSource.removeDoc(docId)
        return if (wasAcknowledged) {
            Result.Success(getAllDocs(username))
        } else {
            Result.Error("Could not create document")
        }
    }

    suspend fun getAllDocs(username: String): List<Document> {
        return docsDataSource.getAllDocs().filter {
            it.access.contains(username)
        }
    }

    suspend fun addUserToDoc(user: OnlineUser, documentId: ID): Result<Document> {
        if (!onlineDocuments.containsKey(documentId)) {
            val document = docsDataSource.getDocById(documentId) ?: return Result.Error("Document not found")
            onlineDocuments[documentId] = DocumentManager(
                document,
                CursorManager().apply {
                    updatePosition(
                        user.username,
                        cursorPosition = CursorPosition(
                            line = 0, column = 0
                        )
                    )
                },
            )
        }

        onlineDocuments[documentId]!!.activeUsers[user.username] = user

        UserManager.changeUserState(user.username, UserState.InDocument(documentId))
        return Result.Success(onlineDocuments[documentId]!!.getDocument())
    }

    suspend fun removeUserFromDoc(username: String): Result<Unit> {
        val user = UserManager.getUser(username)
        if (user == null || user.state !is UserState.InDocument)
            return Result.Error("Document not found")

        val onlineDocument = onlineDocuments[user.state.documentId] ?:
            return Result.Error("Document not found")

        onlineDocument.activeUsers.remove(username)

        if (onlineDocument.activeUsers.size == 0) {
            saveDocument(onlineDocument.getDocument().id)
            onlineDocuments.remove(user.state.documentId)
        }
        UserManager.changeUserState(username, UserState.InMain)
        return Result.Success(Unit)
    }

    suspend fun saveDocument(docId: ID) {
        val onlineDocument = onlineDocuments[docId]?.getDocument() ?: return
        docsDataSource.updateDoc(onlineDocument)
    }
}