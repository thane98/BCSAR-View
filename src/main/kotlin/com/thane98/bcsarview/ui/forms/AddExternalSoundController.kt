package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.lang.Exception
import java.net.URL
import java.util.*

class AddExternalSoundController(private val csar: Csar) : Initializable {
    @FXML
    private lateinit var stage: Stage
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
        configBox.items = csar.configs
    }

    @FXML
    private fun add() {
        if (!validFields())
            showInvalidFieldsAlert()
        else {
            try {
                csar.addExternalSound(
                    nameField.text,
                    pathField.text,
                    playerBox.selectionModel.selectedItem,
                    configBox.selectionModel.selectedItem
                )
                stage.close()
            } catch(ex: Exception) {
                showAddFailureAlert(ex)
            }
        }
    }

    private fun showAddFailureAlert(ex: Exception) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Add Failed"
        alert.headerText = "Unable to add external sound."
        alert.contentText = ex.message
    }

    private fun showInvalidFieldsAlert() {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Add Failed"
        alert.headerText = "Invalid Fields"
        alert.contentText = "Please fill in all fields first."
        applyStyles(alert.dialogPane.scene)
        alert.showAndWait()
    }

    private fun validFields(): Boolean {
        return nameField.text.isNotEmpty()
                && pathField.text.isNotEmpty()
                && playerBox.selectionModel.selectedItem != null
                && configBox.selectionModel.selectedItem != null
    }

    @FXML
    private fun cancel() {
        stage.close()
    }
}