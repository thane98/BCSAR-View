package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.core.structs.entries.InternalFileReference
import com.thane98.bcsarview.ui.MainWindowController
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableView
import java.nio.file.Path

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

    fun updateStatus(text: String) { Platform.runLater { MainWindowController.statusLine.value = text } }
    fun performWithWaitingScreen(action: () -> Unit) { parentForm?.performWithWaitingScreen(action) }
    fun refresh() { table.refresh() }
    fun retrieveEntries(): List<AbstractNamedEntry> { return table.items }

    fun dumpFile(entry: T, record: InternalFileReference, destination: Path) {
        performWithWaitingScreen {
            csar.value.dumpFile(record, destination)
            updateStatus("Successfully dumped $entry to $destination.")
        }
    }

    abstract fun onFileChange(csar: Csar?)
}