package com.dvir.docsync.docs.domain.managers.documents

import com.dvir.docsync.core.constants.Constants
import com.dvir.docsync.core.user.OnlineUser
import com.dvir.docsync.docs.domain.managers.cursor.CursorAction
import com.dvir.docsync.docs.domain.managers.cursor.CursorData
import com.dvir.docsync.docs.domain.managers.cursor.CursorManager
import com.dvir.docsync.docs.domain.managers.cursor.CursorPosition
import com.dvir.docsync.docs.domain.model.Character
import com.dvir.docsync.docs.domain.model.CharacterConfig
import com.dvir.docsync.docs.domain.model.Document
import java.util.concurrent.ConcurrentHashMap

class DocumentManager(
    private val document: Document,
    private val cursorManager: CursorManager,
    val activeUsers: ConcurrentHashMap<String, OnlineUser> = ConcurrentHashMap()
) {
    @Synchronized
    fun addAccess(ownerUsername: String, addedUsername: String) {
        if (ownerUsername != document.owner)
            return

        document.addAccessTo(addedUsername)
    }

    @Synchronized
    fun removeAccess(ownerUsername: String, removedUsername: String) {
        if (ownerUsername != document.owner)
            return

        document.removeAccessTo(removedUsername)
    }

    @Synchronized
    fun updateCursor(username: String, cursorData: CursorData) {
        if (cursorData.end == null) {
            cursorManager.updatePosition(username, cursorData.start)
        } else {
            cursorManager.updateSelection(username, cursorData.start, cursorData.end)
        }
    }

    @Synchronized
    fun addCharacter(character: Character, username: String) {
        val cursorData = cursorManager.getCursors()[username] ?: return
        println("adding character in doc")
        val (startPos, endPos) = cursorData

        if (endPos != null) {
            removeSelection(username, startPos, endPos)
            cursorManager.updatePosition(username, startPos)
        }

        document.addCharacter(startPos, character)

        cursorManager.adjustCursors(startPos, CursorAction.Add)

        val newCursorPosition = indexToPosition(positionToIndex(startPos) + 1)
        cursorManager.updatePosition(username, newCursorPosition)
    }

    @Synchronized
    fun editCharacters(username: String, config: CharacterConfig) {
        val cursorData = cursorManager.getCursors()[username] ?: return
        val (_, endPos) = cursorData

        if (endPos == null)
            return

        document.editCharacters(config, cursorData)
    }

    @Synchronized
    fun removeCharacters(username: String) {
        val cursorData = cursorManager.getCursors()[username] ?: return
        val (startPos, endPos) = cursorData

        if (endPos != null) {
            removeSelection(username, startPos, endPos)
            return
        }

        document.removeCharacter(startPos)
        cursorManager.adjustCursors(startPos, CursorAction.Remove)
    }

    private fun removeSelection(username: String, startPos: CursorPosition, endPos: CursorPosition) {
        val startIndex = positionToIndex(startPos)
        val endIndex = positionToIndex(endPos)

        if (startIndex > endIndex) {
            throw IllegalArgumentException("Start position must be before end position")
        }

        document.content.subList(startIndex, endIndex).clear()

        for (i in startIndex until endIndex) {
            val position = indexToPosition(i)
            cursorManager.adjustCursors(position, CursorAction.Remove)
        }

        cursorManager.updatePosition(username, startPos)
    }

    private fun indexToPosition(index: Int): CursorPosition {
        var line = 0
        var column = 0
        for (i in 0 until index) {
            if (i >= document.content.size) break
            when (document.content[i]) {
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
        }
        return CursorPosition(line, column)
    }

    private fun positionToIndex(position: CursorPosition): Int {
        var index = 0
        var line = 0
        var column = 0
        while (line < position.line || (line == position.line && column < position.column)) {
            if (index >= document.content.size) {
                break
            }
            when (document.content[index]) {
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

    fun getDocument(): Document = document
}