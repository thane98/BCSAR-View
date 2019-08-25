package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.entries.ExternalFileReference
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.*

class EditExternalSoundController(private val record: ExternalFileReference) : Initializable {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var pathField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) { pathField.text = record.path }

    @FXML
    private fun commit() {
        record.path = pathField.text
        stage.close()
    }

    @FXML
    private fun cancel() { stage.close() }
}