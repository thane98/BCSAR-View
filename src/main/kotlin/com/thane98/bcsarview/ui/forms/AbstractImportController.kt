package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.createBcsarOpenDialog
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

abstract class AbstractImportController(protected val destinationCsar: Csar): Initializable {
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
        val dialog = createBcsarOpenDialog()
        val result = dialog.showOpenDialog(stage)
        if (result != null) {
            fileNameField.text = result.name
            sourceCsar = Csar(result.toPath())
            onOpenCsar()
        }
    }

    protected abstract fun onOpenCsar()

    @FXML
    private fun import() {
        try {
            doImport()
            stage.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    protected abstract fun doImport()

    private fun showImportFailureAlert(ex: Exception) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Add Failed"
        alert.headerText = "Unable to import sounds."
        alert.contentText = ex.message
        alert.showAndWait()
    }

    @FXML
    private fun cancel() { stage.close() }
}