package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.createIntegerTextFormatter
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.*

class CreatePlayerController(private val csar: Csar) : AbstractCreateController() {
    @FXML
    private lateinit var nameField: TextField
    @FXML
    private lateinit var soundLimitField: TextField
    @FXML
    private lateinit var unknownField: TextField
    @FXML
    private lateinit var heapSizeField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        soundLimitField.textFormatter = createIntegerTextFormatter()
        unknownField.textFormatter = createIntegerTextFormatter()
        heapSizeField.textFormatter = createIntegerTextFormatter()
        createButton.disableProperty().bind(
            nameField.textProperty().isEmpty
                .or(soundLimitField.textProperty().isEmpty)
                .or(unknownField.textProperty().isEmpty)
                .or(heapSizeField.textProperty().isEmpty)
        )
    }

    @FXML
    override fun createAndInsert(): AbstractNamedEntry {
        val player = Player()
        player.name.value = nameField.text
        player.soundLimit.value = Integer.parseInt(soundLimitField.text)
        player.unknown.value = Integer.parseInt(unknownField.text)
        player.heapSize.value = Integer.parseInt(heapSizeField.text)
        csar.players.add(player)
        return player
    }
}