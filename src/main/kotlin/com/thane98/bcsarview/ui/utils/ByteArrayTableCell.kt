package com.thane98.bcsarview.ui.utils

import com.aneagle.SpecialTextField
import javafx.scene.control.TableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class ByteArrayTableCell<T> : TableCell<T, ByteArray>() {
    init {
        isEditable = true
    }

    override fun updateItem(item: ByteArray?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = null
        if (empty || item == null) {
            text = null
        } else {
            text = byteArrayToText(item)
        }
    }

    override fun startEdit() {
        super.startEdit()
        val editField = SpecialTextField(generateMask())
        editField.forceSetText(text)
        editField.setOnAction {
            try {
                commitEdit(parseByteArrayFromText(editField.text))
            } catch (ex: Exception) {
                cancelEdit()
                ex.printStackTrace()
            }
        }
        editField.addEventFilter(KeyEvent.KEY_PRESSED) { e ->
            if (e.code == KeyCode.ESCAPE) {
                cancelEdit()
                e.consume()
            }
        }
        graphic = editField
        text = null
        editField.requestFocus()
    }

    override fun cancelEdit() {
        super.cancelEdit()
        updateItem(item, isEmpty)
    }

    private fun generateMask(): String {
        val sb = StringBuilder()
        for (i in 0 until item.size) {
            sb.append("HH")
            if (i < item.size - 1)
                sb.append(' ')
        }
        return sb.toString()
    }
}