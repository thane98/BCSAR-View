package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.Player
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class CreateExternalSoundController(private val csar: Csar) : AbstractCreateController() {
    @FXML
    private lateinit var nameField: TextField
    @FXML
    private lateinit var pathField: TextField
    @FXML
    private lateinit var playerBox: ComboBox<Player>
    @FXML
    private lateinit var configBox: ComboBox<AudioConfig>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        playerBox.items = csar.players
        configBox.items = csar.configs.filtered { it.configType == ConfigType.EXTERNAL_SOUND }
        createButton.disableProperty().bind(
            nameField.textProperty().isEmpty
                .or(pathField.textProperty().isEmpty)
                .or(playerBox.valueProperty().isNull)
                .or(configBox.valueProperty().isNull)
        )
    }

    override fun createAndInsert(): AbstractNamedEntry {
        csar.createExternalSound(
            nameField.text,
            pathField.text,
            playerBox.selectionModel.selectedItem,
            configBox.selectionModel.selectedItem
        )
        return csar.configs.last()
    }
}