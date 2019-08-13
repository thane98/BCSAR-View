package com.thane98.bcsarview.ui.utils

import javafx.scene.control.TableCell
import javafx.stage.Stage

class DialogTableCell<T, U>(private val dialog: Stage, private val setter: (U) -> Unit): TableCell<T, U>() {
    init {
        isEditable = true
        dialog.setOnCloseRequest { commitEdit(item) }
    }

    override fun updateItem(item: U, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = null
        if (empty || item == null) {
            text = null
        } else {
            text = "Click to edit..."
        }
    }

    override fun startEdit() {
        super.startEdit()
        setter.invoke(item)
        dialog.showAndWait()
    }

    override fun cancelEdit() {
        super.cancelEdit()
        updateItem(item, isEmpty)
    }
}