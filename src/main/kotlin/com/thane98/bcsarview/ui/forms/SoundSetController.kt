package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.ui.Main
import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
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

    val csar = SimpleObjectProperty<Csar>()

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
            val dumpItem = MenuItem("Dump")
            val extractItem = MenuItem("Extract Sounds")
            val editItem = MenuItem("Edit Contents")
            contextMenu.items.addAll(dumpItem, extractItem, editItem)
            dumpItem.setOnAction { dumpSoundSet(row.item) }
            extractItem.setOnAction { extractSoundSet(row.item) }
            editItem.setOnAction { openSoundSetEditor(row.item) }
            row.contextMenu = contextMenu
            row
        }
    }

    private fun openSoundSetEditor(soundSet: SoundSet) {
        val loader = FXMLLoader()
        loader.setController(SoundSetEditorController(csar.value, soundSet))
        val stage = loader.load<Stage>(Main::class.java.getResourceAsStream("SoundSetEditor.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
        table.refresh()
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

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.soundSets) }
}