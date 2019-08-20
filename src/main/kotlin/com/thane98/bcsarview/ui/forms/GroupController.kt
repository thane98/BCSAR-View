package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.SoundGroup
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

class GroupController : AbstractEntryController<SoundGroup>() {
    @FXML
    private lateinit var nameColumn: TableColumn<SoundGroup, String>
    @FXML
    private lateinit var unknownColumn: TableColumn<SoundGroup, Number>

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<SoundGroup>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            contextMenu.items.addAll(dumpItem)
            dumpItem.setOnAction { dumpSoundGroup(row.item) }
            row.contextMenu = contextMenu
            row
        }
    }

    private fun dumpSoundGroup(soundGroup: SoundGroup) {
        val chooser = createDumpSoundGroupDialog(soundGroup)
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            csar.value.dumpFile(soundGroup.file.value, result.toPath())
    }

    private fun createDumpSoundGroupDialog(soundGroup: SoundGroup): FileChooser {
        val chooser = FileChooser()
        chooser.title = "Save sound group..."
        chooser.initialFileName = "$soundGroup.cgrp"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Group", "*.cgrp"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        return chooser
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.groups) }
}