package com.thane98.bcsarview.ui.forms

import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableView

abstract class AbstractEntryController<T> : Initializable {
    @FXML
    protected lateinit var table : TableView<T>

    fun applyFilter(text: String) {
        val items = table.items
        if (items is FilteredList<T>) {
            items.setPredicate { it.toString().contains(text) }
        }
    }
}