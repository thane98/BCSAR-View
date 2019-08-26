package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.MainWindowController
import com.thane98.bcsarview.ui.utils.Dialogs
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.lang.Exception
import java.net.URL
import java.util.*

abstract class AbstractImportController(protected val destinationCsar: Csar): AbstractFormController() {
    @FXML
    protected lateinit var stage: Stage
    @FXML
    private lateinit var fileNameField: TextField
    @FXML
    protected lateinit var playerBox: ComboBox<Player>
    @FXML
    protected lateinit var importButton: Button

    protected var sourceCsar: Csar? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        playerBox.items = destinationCsar.players
    }

    @FXML
    private fun openCsar() {
        val dialog = Dialogs.bcsarChooser
        val result = dialog.showOpenDialog(stage)
        if (result != null) {
            dialog.initialDirectory = result.parentFile
            fileNameField.text = result.name
            sourceCsar = Csar(result.toPath())
            onOpenCsar()
        }
    }

    protected abstract fun onOpenCsar()

    @FXML
    private fun import() {
        performWithWaitingScreen {
            doImport()
            Platform.runLater {
                MainWindowController.statusLine.value = "Import completed successfully."
                stage.close()
            }
        }
    }

    protected abstract fun doImport()

    @FXML
    private fun cancel() { stage.close() }
}