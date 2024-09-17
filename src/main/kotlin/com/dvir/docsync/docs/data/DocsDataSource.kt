package com.dvir.docsync.docs.data

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.model.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class DocsDataSource(
    db: CoroutineDatabase
) {
    private val docs = db.getCollection<Document>()

    suspend fun getDoc(id: ID): Document? {
        return docs.findOne(Document::id eq id)
    }

    suspend fun getAllDocs(): List<Document> {
        return docs.find().toList()
    }

    suspend fun insertDoc(doc: Document): Boolean {
        return docs.insertOne(doc).wasAcknowledged()
    }

    suspend fun updateDoc(doc: Document): Boolean {
        return docs.updateOneById(doc.id, doc).wasAcknowledged()
    }
}