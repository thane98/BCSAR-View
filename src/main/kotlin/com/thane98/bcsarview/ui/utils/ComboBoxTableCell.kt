package com.thane98.bcsarview.ui.utils

import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.TableCell

class ComboBoxTableCell<T, U>(items: ObservableList<U>) : TableCell<T, U>() {
    private val comboBox = ComboBox<U>()

    init {
        comboBox.items = items
        comboBox.selectionModel.selectedItemProperty().addListener { _ ->
            val newItem = comboBox.selectionModel.selectedItem
            this.
            updateItem(newItem, newItem == null)
        }
    }

    override fun updateItem(item: U, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        graphic = if (empty || item == null) null else comboBox
        comboBox.selectionModel.select(item)
    }
}