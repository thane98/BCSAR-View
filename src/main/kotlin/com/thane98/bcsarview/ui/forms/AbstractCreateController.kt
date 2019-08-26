package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.ui.MainWindowController
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.stage.Stage

abstract class AbstractCreateController : Initializable {
    @FXML
    protected lateinit var stage: Stage
    @FXML
    protected lateinit var createButton: Button

    @FXML
    private fun create() {
        val entry = createAndInsert()
        MainWindowController.statusLine.value = "Created $entry."
        stage.close()
    }

    protected abstract fun createAndInsert(): AbstractNamedEntry

    @FXML
    private fun cancel() {
        stage.close()
    }
}