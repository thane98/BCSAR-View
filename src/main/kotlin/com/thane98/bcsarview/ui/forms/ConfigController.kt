package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.ExternalFileReference
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.HexAreaTableCell
import com.thane98.bcsarview.ui.utils.loadAndShowForm
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
import java.net.URL
import java.util.*

class ConfigController : AbstractEntryController<AudioConfig>() {
    @FXML
    private lateinit var nameColumn: TableColumn<AudioConfig, String>
    @FXML
    private lateinit var typeColumn: TableColumn<AudioConfig, String>
    @FXML
    private lateinit var playerColumn: TableColumn<AudioConfig, Player>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<AudioConfig, ByteArray>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        typeColumn.setCellValueFactory { SimpleStringProperty(it.value.configType.name) }
        playerColumn.setCellValueFactory { it.value.player }
        playerColumn.setCellFactory {
            ComboBoxTableCell.forTableColumn<AudioConfig, Player>(csar.value.players).call(playerColumn)
        }
        unknownThreeColumn.setCellValueFactory { it.value.unknownThree }
        unknownThreeColumn.setCellFactory { HexAreaTableCell<AudioConfig>() }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<AudioConfig>()
            row.setOnContextMenuRequested { e ->
                if (row.item != null) {
                    when (row.item.configType) {
                        ConfigType.EXTERNAL_SOUND -> createExternalSoundContextMenu(row).show(row, e.screenX, e.screenY)
                        ConfigType.INTERNAL_SOUND -> createInternalSoundContextMenu(row).show(row, e.screenX, e.screenY)
                        ConfigType.SEQUENCE -> createSequenceContextMenu(row).show(row, e.screenX, e.screenY)
                    }
                }
            }
            row
        }
    }

    private fun createInternalSoundContextMenu(row: TableRow<AudioConfig>): ContextMenu {
        val contextMenu = ContextMenu()
        val dumpMenu = createDumpMenu(row)
        val massEditMenu = createMassEditMenu()
        contextMenu.items.addAll(massEditMenu, dumpMenu)
        return contextMenu
    }

    private fun createDumpMenu(row: TableRow<AudioConfig>): Menu {
        val dumpMenu = Menu("Dump")
        val dumpToCwavItem = MenuItem("To CWAV")
        val dumpToWavItem = MenuItem("To WAV")
        dumpToCwavItem.setOnAction { dumpSound(row.item, false) }
        dumpToWavItem.setOnAction { dumpSound(row.item, true) }
        dumpToWavItem.disableProperty().bind(Configuration.cwavToWavCommand.isEmpty)
        dumpMenu.items.addAll(dumpToCwavItem, dumpToWavItem)
        return dumpMenu
    }

    private fun createExternalSoundContextMenu(row: TableRow<AudioConfig>): ContextMenu {
        val contextMenu = ContextMenu()
        val editPathItem = MenuItem("Edit Path")
        val massEditMenu = createMassEditMenu()
        editPathItem.setOnAction {
            assert(row.item.configType == ConfigType.EXTERNAL_SOUND)
            loadAndShowForm(
                "EditExternalSound.fxml",
                EditExternalSoundController(row.item.file.value as ExternalFileReference)
            )
        }
        contextMenu.items.addAll(massEditMenu, editPathItem)
        return contextMenu
    }

    private fun createSequenceContextMenu(row: TableRow<AudioConfig>): ContextMenu {
        val contextMenu = ContextMenu()
        val dumpItem = MenuItem("Dump")
        dumpItem.setOnAction { dumpSequence(row.item) }
        contextMenu.items.add(dumpItem)
        return contextMenu
    }

    private fun createMassEditMenu(): Menu {
        val menu = Menu("Mass Edit")
        val massEditPlayersItem = MenuItem("Players")
        val massEditConfigsItem = MenuItem("Configs")
        massEditPlayersItem.setOnAction { openMassEditPlayers() }
        massEditConfigsItem.setOnAction { openMassEditConfigs() }
        menu.items.addAll(massEditPlayersItem, massEditConfigsItem)
        return menu
    }

    private fun openMassEditPlayers() {
        loadAndShowForm("MassEdit.fxml", MassEditPlayersForSoundsController(csar.value))
    }

    private fun openMassEditConfigs() {
        loadAndShowForm("MassEdit.fxml", MassEditExtendedConfigsController(csar.value))
    }

    private fun dumpSound(config: AudioConfig, convertToWav: Boolean) {
        val soundFilter = if (convertToWav)
            FileChooser.ExtensionFilter("Wave Archive", "*.wav")
        else
            FileChooser.ExtensionFilter("3DS Wave Archive", "*.cwav")
        val chooser = FileChooser()
        chooser.title = "Save sound..."
        chooser.initialFileName = if (convertToWav) "$config.wav" else "$config.cwav"
        chooser.extensionFilters.addAll(
            soundFilter,
            FileChooser.ExtensionFilter("All Files", "*.*")
        )

        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            performWithWaitingScreen { csar.value.dumpSound(config, result.toPath(), convertToWav) }
    }

    private fun dumpSequence(config: AudioConfig) {
        val chooser = FileChooser()
        chooser.title = "Save sequence..."
        chooser.initialFileName = "$config.cseq"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sequences", "*.cseq"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            performWithWaitingScreen { csar.value.dumpSound(config, result.toPath(), false) }
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.configs) }
}