package com.dvir.docsync.docs.data

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.model.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class DocsDataSource(
    db: CoroutineDatabase
) {
    private val docs = db.getCollection<Document>()

    suspend fun getDocById(id: ID): Document? {
        return docs.findOne(Document::id eq id)
    }

    suspend fun getAllDocs(): List<Document> {
        return docs.find().toList()
    }

    suspend fun removeDoc(docId: ID): Boolean {
        return docs.deleteOne(Document::id eq docId).wasAcknowledged()
    }

    suspend fun insertDoc(doc: Document): Boolean {
        return docs.insertOne(doc).wasAcknowledged()
    }

    suspend fun updateDoc(doc: Document): Boolean {
        return docs.replaceOne(Document::id eq doc.id, doc).wasAcknowledged()
    }
}