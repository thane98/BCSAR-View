package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Archive
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.createBcsarOpenDialog
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.Stage
import java.lang.Exception
import java.net.URL
import java.util.*

class ImportExternalSoundsController(private val destinationCsar: Csar): Initializable {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var fileNameField: TextField
    @FXML
    private lateinit var playerBox: ComboBox<Player>
    @FXML
    private lateinit var externalSoundSelectionView: ListView<AudioConfig>
    @FXML
    private lateinit var importButton: Button

    private var sourceCsar: Csar? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        externalSoundSelectionView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        playerBox.items = destinationCsar.players
        val hasNoSelectionProperty = Bindings.isEmpty(externalSoundSelectionView.selectionModel.selectedItems)
        val noPlayerProperty = Bindings.isNull(playerBox.selectionModel.selectedItemProperty())
        importButton.disableProperty().bind(hasNoSelectionProperty.or(noPlayerProperty))
    }

    @FXML
    private fun openCsar() {
        val dialog = createBcsarOpenDialog()
        val result = dialog.showOpenDialog(stage)
        if (result != null) {
            fileNameField.text = result.name
            sourceCsar = Csar(result.toPath())
            externalSoundSelectionView.items = sourceCsar?.configs?.filtered { it.configType == ConfigType.EXTERNAL_SOUND }
        }
    }

    @FXML
    private fun import() {
        try {
//            destinationCsar.importExternalSounds(
//                externalSoundSelectionView.selectionModel.selectedItems,
//                playerBox.selectionModel.selectedItem
//            )
            stage.close()
        } catch (ex: Exception) {
            showImportFailureAlert(ex)
        }
    }

    private fun showImportFailureAlert(ex: Exception) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Add Failed"
        alert.headerText = "Unable to import sounds."
        alert.contentText = ex.message
    }

    @FXML
    private fun cancel() {
        stage.close()
    }
}