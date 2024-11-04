package com.dvir.docsync.docs.domain.model

import com.dvir.docsync.core.model.ID
import com.dvir.docsync.docs.domain.managers.cursor.CursorData
import com.dvir.docsync.docs.domain.managers.cursor.CursorPosition
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

/**
 * Represents a document with editable content and access control.
 * @property access A list of usernames that have access to this document.
 */
@Serializable
data class Document(
    @BsonId
    val id: ID = ObjectId().toString(),
    val owner: String,
    val name: String,
    val creationDate: Long,
    val editedDetails: MutableList<String>,
    val access: MutableList<String>,
    val content: MutableList<Character>,
) {
    fun changeEditDate(username: String, time: Long) {
        if (editedDetails.size == 2) {
            editedDetails[0] = username
            editedDetails[1] = time.toString()
        } else {
            editedDetails.add(username)
            editedDetails.add(time.toString())
        }
    }

    fun addAccessTo(username: String) {
        access.add(username)
    }

    fun removeAccessTo(username: String) {
        access.remove(username)
    }

    fun editCharacters(characterConfig: CharacterConfig, cursorData: CursorData) {
        if (cursorData.end == null) return

        val start = positionToIndex(cursorData.start)
        val end = positionToIndex(cursorData.end)

        val sublist = content.subList(start, end)

        for (i in sublist.indices) {
            val character = sublist[i]
            if (character is Character.Visible) {
                sublist[i] = character.copy(
                    config = CharacterConfig(
                        isBold = characterConfig.isBold,
                        isItalic = characterConfig.isItalic,
                        isUnderlined = characterConfig.isUnderlined,
                        color = characterConfig.color,
                        fontSize = characterConfig.fontSize
                    )
                )
            }
        }
    }

    fun addCharacter(position: CursorPosition, character: Character) {
        val index = positionToIndex(position)
        content.add(index, character)
    }


    fun removeCharacter(position: CursorPosition) {
        val index = positionToIndex(position)
        content.removeAt(index)
    }

    private fun positionToIndex(position: CursorPosition): Int {
        var index = 0
        var line = 0
        var column = 0
        while (line < position.line || (line == position.line && column < position.column)) {
            if (index >= content.size) {
                break
            }
            when (content[index]) {
                is Character.BreakLine -> {
                    line++
                    column = 0
                }
                else -> column++
            }
            index++
        }
        return index
    }
}