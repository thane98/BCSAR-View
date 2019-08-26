package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.loadAndShowForm
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class SoundSetController : AbstractEntryController<SoundSet>() {
    @FXML
    private lateinit var nameColumn: TableColumn<SoundSet, String>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<SoundSet, Number>
    @FXML
    private lateinit var archiveColumn: TableColumn<SoundSet, String>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        unknownThreeColumn.setCellValueFactory { it.value.unknownThree }
        unknownThreeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        archiveColumn.setCellValueFactory { SimpleStringProperty(it.value.archive.value?.toString()) }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<SoundSet>()
            val contextMenu = ContextMenu()
            val extractMenu = createExtractMenu(row)
            val dumpItem = MenuItem("Dump")
            val editItem = MenuItem("Edit Contents")
            dumpItem.setOnAction { dumpSoundSet(row.item) }
            editItem.setOnAction { openSoundSetEditor(row.item) }
            contextMenu.items.addAll(extractMenu, dumpItem, editItem)
            row.contextMenu = contextMenu
            row
        }
    }

    private fun createExtractMenu(row: TableRow<SoundSet>): Menu {
        val menu = Menu("Extract")
        val extractToCwavItem = MenuItem("To CWAV")
        val extractToWavItem = MenuItem("To WAV")
        extractToCwavItem.setOnAction { extractSoundSet(row.item, false) }
        extractToWavItem.setOnAction { extractSoundSet(row.item, true) }
        extractToWavItem.disableProperty().bind(Configuration.cwavToWavCommand.isEmpty)
        menu.items.addAll(extractToCwavItem, extractToWavItem)
        return menu
    }

    private fun openSoundSetEditor(soundSet: SoundSet) {
        loadAndShowForm("SoundSetEditor.fxml", SoundSetEditorController(csar.value, soundSet))
        table.refresh()
    }

    private fun dumpSoundSet(soundSet: SoundSet) {
        val chooser = createDumpSoundSetDialog(soundSet)
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            dumpFile(soundSet, soundSet.file.value, result.toPath())
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

    private fun extractSoundSet(soundSet: SoundSet, convertToWav: Boolean) {
        if (soundSet.archive.value == null) {
            showInvalidExtractionDialog()
        } else {
            val chooser = DirectoryChooser()
            chooser.title = "Select destination..."
            val result = chooser.showDialog(table.scene.window)
            if (result != null) {
                performWithWaitingScreen {
                    csar.value.extractSoundSet(soundSet, result.toPath(), convertToWav)
                    updateStatus("Extracted ${soundSet.sounds.size} sounds to ${result.path}.")
                }
            }
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

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.soundSets) }
}