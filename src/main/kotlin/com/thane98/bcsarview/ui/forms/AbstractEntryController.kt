package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableView

abstract class AbstractEntryController<T : AbstractNamedEntry> : Initializable {
    @FXML
    protected lateinit var table : TableView<T>
    var parentForm: AbstractFormController? = null
    val csar = SimpleObjectProperty<Csar>()

    init {
        csar.addListener { _ -> onFileChange(csar.value) }
    }

    fun applyFilter(text: String) {
        val items = table.items
        if (items is FilteredList<T>) {
            items.setPredicate { it.toString().contains(text) }
        }
    }

    fun performWithWaitingScreen(action: () -> Unit) { parentForm?.performWithWaitingScreen(action) }

    fun refresh() { table.refresh() }

    fun retrieveEntries(): List<AbstractNamedEntry> { return table.items }

    abstract fun onFileChange(csar: Csar?)
}