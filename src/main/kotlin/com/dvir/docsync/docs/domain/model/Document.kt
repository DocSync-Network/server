package com.dvir.docsync.docs.domain.model

import com.dvir.docsync.core.constants.Constants
import com.dvir.docsync.docs.domain.managers.cursor.CursorPosition
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Document(
    @BsonId
    val id: String = ObjectId().toString(),
    val owner: String,
    val name: String,
    val creationDate: Long,
    val content: MutableList<Character> = mutableListOf()
) {
    fun addCharacter(position: CursorPosition, character: Character) {
        val index = positionToIndex(position)
        content.add(index, character)
    }

    fun removeCharacters(position: CursorPosition, length: Int) {
        val index = positionToIndex(position)
        if (index - length < 0) {
            throw IllegalArgumentException("Cannot remove more characters than available before cursor")
        }
        val fromIndex = index - length
        content.subList(fromIndex, index).clear()
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
                else -> {
                    column++
                    if (column == Constants.LINE_LENGTH) {
                        line++
                        column = 0
                    }
                }
            }
            index++
        }
        return index
    }
}
