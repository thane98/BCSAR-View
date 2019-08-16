package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import com.thane98.bcsarview.ui.utils.HexAreaTableCell
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
import javafx.util.converter.NumberStringConverter
import java.lang.IllegalArgumentException
import java.net.URL
import java.util.*


class ConfigController : Initializable {
    @FXML
    private lateinit var table: TableView<AudioConfig>
    @FXML
    private lateinit var nameColumn: TableColumn<AudioConfig, String>
    @FXML
    private lateinit var playerColumn: TableColumn<AudioConfig, String>
    @FXML
    private lateinit var unknownColumn: TableColumn<AudioConfig, Number>
    @FXML
    private lateinit var unknownTwoColumn: TableColumn<AudioConfig, ByteArray>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<AudioConfig, ByteArray>

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory {SimpleStringProperty(it.value.toString()) }
        playerColumn.setCellValueFactory { SimpleStringProperty(it.value.player.value.toString()) }
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        unknownTwoColumn.setCellValueFactory { it.value.unknownTwo }
        unknownTwoColumn.setCellFactory { ByteArrayTableCell<AudioConfig>() }
        unknownThreeColumn.setCellValueFactory { it.value.unknownThree }
        unknownThreeColumn.setCellFactory { HexAreaTableCell<AudioConfig>() }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<AudioConfig>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            contextMenu.items.addAll(dumpItem)
            dumpItem.setOnAction { dumpSound(row.item) }
            row.setOnContextMenuRequested { e ->
                if (row.item != null && row.item.configType != ConfigType.EXTERNAL_SOUND)
                    contextMenu.show(row, e.screenX, e.screenY)
            }
            row
        }
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

    fun onFileChange(csar: Csar?) { table.items = csar?.configs }
}