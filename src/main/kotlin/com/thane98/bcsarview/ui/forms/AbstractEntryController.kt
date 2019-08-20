package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableView

abstract class AbstractEntryController<T : AbstractNamedEntry> : Initializable {
    @FXML
    protected lateinit var table : TableView<T>

    fun applyFilter(text: String) {
        val items = table.items
        if (items is FilteredList<T>) {
            items.setPredicate { it.toString().contains(text) }
        }
    }

    fun refresh() { table.refresh() }

    fun retrieveEntries(): List<AbstractNamedEntry> { return table.items }

    abstract fun onFileChange(csar: Csar?)
}