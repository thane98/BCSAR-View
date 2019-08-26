package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.ui.MainWindowController
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.*

class MassRenameController(private val entries: List<AbstractNamedEntry>) : AbstractFormController() {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var fromField: TextField
    @FXML
    private lateinit var toField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) {}

    fun commit() {
        performWithWaitingScreen {
            for (entry in entries)
                entry.name.value = entry.name.value.replace(fromField.text, toField.text)
            Platform.runLater {
                MainWindowController.statusLine.value = "Mass rename complete."
                stage.close()
            }
        }
    }

    fun cancel() { stage.close() }
}