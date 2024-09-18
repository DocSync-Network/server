package com.dvir.docsync.docs.domain.managers.documents

import com.dvir.docsync.core.user.UserManager
import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.data.DocsDataSource
import com.dvir.docsync.docs.domain.managers.cursor.CursorManager
import com.dvir.docsync.core.user.UserState
import com.dvir.docsync.docs.domain.model.Document
import com.dvir.docsync.core.model.Result
import java.util.concurrent.ConcurrentHashMap

class DocumentsManager(
    private val docsDataSource: DocsDataSource
) {
    private val onlineDocuments = ConcurrentHashMap<ID, DocumentManager>()

    suspend fun createDoc(username: String, docName: String): Result<Document> {
        val document = Document(
            owner = username,
            name = docName,
            creationDate = System.currentTimeMillis(),
            access = mutableListOf(username)
        )
        val wasAcknowledged = docsDataSource.insertDoc(document)
        return if (wasAcknowledged) {
            addUserToDoc(username, document.id)
        } else {
            Result.Error("Could not create document")
        }
    }

    suspend fun removeDoc(username: String, docId: ID): Result<List<Document>> {
        val document = docsDataSource.getDocById(docId) ?: return Result.Error("Document not found")
        if (document.owner != username) {
            return Result.Error("You are not allowed to delete this document")
        }

        println("Removing $docId")
        val wasAcknowledged = docsDataSource.removeDoc(docId)
        return if (wasAcknowledged) {
            println("Removed")
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

    suspend fun addUserToDoc(username: String, documentId: ID): Result<Document> {
        if (!onlineDocuments.containsKey(documentId)) {
            val document = docsDataSource.getDocById(documentId) ?: return Result.Error("Document not found")
            onlineDocuments[documentId] = DocumentManager(document, CursorManager())
        }

        onlineDocuments[documentId]!!.activeUsers++

        UserManager.changeUserState(username, UserState.InDocument(documentId))
        return Result.Success(onlineDocuments[documentId]!!.getDocument())
    }

    suspend fun removeUserFromDoc(username: String): Result<Unit> {
        val user = UserManager.getUser(username)
        if (user == null || user.state !is UserState.InDocument)
            return Result.Error("Document not found")

        val onlineDocument = onlineDocuments[user.state.documentId] ?:
            return Result.Error("Document not found")

        onlineDocument.activeUsers--

        if (onlineDocument.activeUsers == 0) {
            docsDataSource.updateDoc(onlineDocument.getDocument())
            onlineDocuments.remove(user.state.documentId)
        }
        UserManager.changeUserState(username, UserState.InMain)
        return Result.Success(Unit)
    }
}