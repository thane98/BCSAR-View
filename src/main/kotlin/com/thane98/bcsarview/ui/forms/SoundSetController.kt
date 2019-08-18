package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class SoundSetController : AbstractEntryController<SoundSet>() {
    @FXML
    private lateinit var nameColumn: TableColumn<SoundSet, StrgEntry>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<SoundSet, Number>
    @FXML
    private lateinit var archiveColumn: TableColumn<SoundSet, String>

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.strgEntry }
        nameColumn.setCellFactory { StrgEntryTableCell<SoundSet>() }
        unknownThreeColumn.setCellValueFactory { it.value.unknownThree }
        unknownThreeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        archiveColumn.setCellValueFactory { SimpleStringProperty(it.value.archive.value?.toString()) }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<SoundSet>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            val extractItem = MenuItem("Extract Sounds")
            contextMenu.items.addAll(dumpItem, extractItem)
            dumpItem.setOnAction { dumpSoundSet(row.item) }
            extractItem.setOnAction { extractSoundSet(row.item) }
            row.contextMenu = contextMenu
            row
        }
    }

    private fun dumpSoundSet(soundSet: SoundSet) {
        val chooser = createDumpSoundSetDialog(soundSet)
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            csar.value.dumpFile(soundSet.file.value, result.toPath())
    }

    private fun createDumpSoundSetDialog(soundSet: SoundSet): FileChooser {
        val chooser = FileChooser()
        chooser.title = "Save set..."
        chooser.initialFileName = "$soundSet.cwsd"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Sets", "*.cwsd"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        return chooser
    }

    private fun extractSoundSet(soundSet: SoundSet) {
        if (soundSet.archive.value == null) {
            showInvalidExtractionDialog()
        } else {
            val chooser = DirectoryChooser()
            chooser.title = "Select destination..."
            val result = chooser.showDialog(table.scene.window)
            if (result != null)
                csar.value.extractSoundSet(soundSet, result.toPath())
        }
    }

    private fun showInvalidExtractionDialog() {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Extraction Failed"
        alert.headerText = "Error: No Archive"
        alert.contentText = "Cannot extract sounds from a sound set with no archive."
        alert.dialogPane.prefWidth = 450.0
        applyStyles(alert.dialogPane.scene)
        alert.showAndWait()
    }

    fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.soundSets) }
}