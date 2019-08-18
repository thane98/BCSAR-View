package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class StrgEntryTableCell<T> : TableCell<T, StrgEntry>() {
    init {
        isEditable = true
    }

    override fun updateItem(entry: StrgEntry?, empty: Boolean) {
        super.updateItem(entry, empty)
        graphic = null
        if (empty || entry == null) {
            text = null
        } else {
            text = entry.name
        }
    }

    override fun startEdit() {
        if (item == null)
            return
        super.startEdit()
        val editor = TextField()
        editor.text = item?.name
        editor.setOnAction {
            if (editor.text.isNotEmpty()) {
                item.name = editor.text
                commitEdit(item)
            } else {
                cancelEdit()
            }
        }
        editor.addEventFilter(KeyEvent.KEY_PRESSED) { e ->
            if (e.code == KeyCode.ESCAPE) {
                cancelEdit()
                e.consume()
            }
        }
        text = null
        graphic = editor
    }

    override fun cancelEdit() {
        super.cancelEdit()
        updateItem(item, isEmpty)
    }
}