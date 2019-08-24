package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.Main
import com.thane98.bcsarview.ui.utils.ComboBoxTableCell
import com.thane98.bcsarview.ui.utils.HexAreaTableCell
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.converter.NumberStringConverter
import java.lang.IllegalArgumentException
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

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        typeColumn.setCellValueFactory { SimpleStringProperty(it.value.configType.name) }
        playerColumn.setCellValueFactory { it.value.player }
        playerColumn.setCellFactory { ComboBoxTableCell<AudioConfig, Player>(csar.value.players) }
        unknownThreeColumn.setCellValueFactory { it.value.unknownThree }
        unknownThreeColumn.setCellFactory { HexAreaTableCell<AudioConfig>() }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<AudioConfig>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            val massEditMenu = Menu("Mass Edit")
            val massEditPlayersItem = MenuItem("Players")
            val massEditConfigsItem = MenuItem("Configs")
            dumpItem.setOnAction { dumpSound(row.item) }
            massEditPlayersItem.setOnAction { openMassEditPlayers() }
            massEditConfigsItem.setOnAction { openMassEditConfigs() }
            massEditMenu.items.addAll(massEditPlayersItem, massEditConfigsItem)
            contextMenu.items.addAll(dumpItem, massEditMenu)
            row.setOnContextMenuRequested { e ->
                if (row.item != null && row.item.configType != ConfigType.EXTERNAL_SOUND)
                    contextMenu.show(row, e.screenX, e.screenY)
            }
            row
        }
    }

    private fun openMassEditPlayers() {
        val loader = FXMLLoader()
        loader.setController(MassEditPlayersForSoundsController(csar.value))
        val stage = loader.load<Stage>(Main::class.java.getResourceAsStream("MassEdit.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }

    private fun openMassEditConfigs() {
        val loader = FXMLLoader()
        loader.setController(MassEditExtendedConfigsController(csar.value))
        val stage = loader.load<Stage>(Main::class.java.getResourceAsStream("MassEdit.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }

    private fun dumpSound(config: AudioConfig) {
        val chooser = FileChooser()
        chooser.title = "Save sound..."
        chooser.initialFileName = when (config.configType) {
            ConfigType.INTERNAL_SOUND -> "$config.cwav"
            ConfigType.SEQUENCE -> "$config.cseq"
            else -> throw IllegalArgumentException("Cannot dump external sound.")
        }
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sounds", "*.cwav", "*.cseq"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )

        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            csar.value.dumpSound(config, result.toPath())
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.configs) }
}