package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.Archive
import com.thane98.bcsarview.ui.MainWindowController
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class ArchiveController : AbstractEntryController<Archive>() {
    @FXML
    private lateinit var nameColumn: TableColumn<Archive, String>
    @FXML
    private lateinit var unknownColumn: TableColumn<Archive, Number>
    @FXML
    private lateinit var entryCountColumn: TableColumn<Archive, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        entryCountColumn.setCellValueFactory { it.value.entryCount }
        entryCountColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<Archive>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            contextMenu.items.addAll(dumpItem)
            dumpItem.setOnAction { dumpArchive(row.item) }
            row.contextMenu = contextMenu
            row
        }
    }

    private fun dumpArchive(archive: Archive) {
        val chooser = createDumpArchiveDialog(archive)
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            dumpFile(archive, archive.file.value, result.toPath())
    }

    private fun createDumpArchiveDialog(archive: Archive): FileChooser {
        val chooser = FileChooser()
        chooser.title = "Save archive..."
        chooser.initialFileName = "$archive.cwar"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Archives", "*.cwar"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        return chooser
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.archives) }
}