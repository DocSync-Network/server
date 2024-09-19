package com.dvir.docsync.docs.domain.managers.cursor

import com.dvir.docsync.core.constants.Constants
import java.util.concurrent.ConcurrentHashMap

class CursorManager {
    private val cursors = ConcurrentHashMap<String, CursorData>()

    fun getCursors(): ConcurrentHashMap<String, CursorData> = cursors


    fun updatePosition(username: String, cursorPosition: CursorPosition) {
        cursors[username] = CursorData(start = cursorPosition, end = null)
    }

    fun updateSelection(username: String, start: CursorPosition, end: CursorPosition) {
        cursors[username] = CursorData(start = start, end = end)
    }

    fun adjustCursors(position: CursorPosition, action: CursorAction) {
        cursors.forEach { (username, cursorData) ->
            if (cursorData.end == null) {
                val updatedCursor = adjustPosition(cursorData.start, position, action)
                if (updatedCursor != null) {
                    cursors[username] = cursorData.copy(start = updatedCursor)
                }
            } else {
                val updatedStart = adjustPosition(cursorData.start, position, action)
                val updatedEnd = adjustPosition(cursorData.end, position, action)

                cursors[username] = CursorData(
                    start = updatedStart ?: cursorData.start,
                    end = updatedEnd ?: cursorData.end
                )
            }
        }
    }

    private fun adjustPosition(
        cursorPosition: CursorPosition,
        actionPosition: CursorPosition,
        action: CursorAction
    ): CursorPosition? {
        if (cursorPosition.line != actionPosition.line) {
            return null
        }

        return when (action) {
            CursorAction.Add -> {
                if (cursorPosition.column < actionPosition.column) {
                    null
                } else {
                    val newColumn = cursorPosition.column + 1
                    if (newColumn == Constants.LINE_LENGTH) {
                        CursorPosition(line = cursorPosition.line + 1, column = 0)
                    } else {
                        cursorPosition.copy(column = newColumn)
                    }
                }
            }
            CursorAction.Remove -> {
                if (cursorPosition.column <= actionPosition.column) {
                    null
                } else {
                    val newColumn = cursorPosition.column - 1
                    if (newColumn < 0) {
                        CursorPosition(line = cursorPosition.line - 1, column = Constants.LINE_LENGTH)
                    } else {
                        cursorPosition.copy(column = newColumn)
                    }
                }
            } else -> null
        }
    }
}
