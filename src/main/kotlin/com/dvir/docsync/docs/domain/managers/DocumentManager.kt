package com.dvir.docsync.docs.domain.managers

import com.dvir.docsync.core.constants.Constants
import com.dvir.docsync.docs.domain.managers.cursor.CursorAction
import com.dvir.docsync.docs.domain.managers.cursor.CursorManager
import com.dvir.docsync.docs.domain.managers.cursor.CursorPosition
import com.dvir.docsync.docs.domain.model.Character
import com.dvir.docsync.docs.domain.model.Document

class DocumentManager(
    private val document: Document,
    private val cursorManager: CursorManager,
) {
    @Synchronized
    fun addCharacter(character: Character, username: String) {
        val cursorData = cursorManager.getCursors()[username] ?: return
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
    fun removeCharacter(username: String, length: Int) {
        val cursorData = cursorManager.getCursors()[username] ?: return
        val (startPos, endPos) = cursorData

        if (endPos != null) {
            removeSelection(username, startPos, endPos)
            return
        }

        document.removeCharacters(startPos, length)

        val removeIndex = positionToIndex(startPos) - length
        val removePosition = indexToPosition(removeIndex)
        cursorManager.adjustCursors(removePosition, CursorAction.Remove)

        val newCursorPosition = indexToPosition(removeIndex)
        cursorManager.updatePosition(username, newCursorPosition)
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

    private fun insertCharacter(character: Character, position: CursorPosition, username: String) {
        val insertIndex = positionToIndex(position)

       document.content.add(insertIndex, character)

        val actionPosition = CursorPosition(line = position.line, column = position.column)
        cursorManager.adjustCursors(actionPosition, CursorAction.Add)

        val newCursorPosition = indexToPosition(insertIndex + 1)
        cursorManager.updatePosition(username, newCursorPosition)
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
