package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage

class MassRenameController(private val entries: List<StrgEntry>) {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var fromField: TextField
    @FXML
    private lateinit var toField: TextField

    fun commit() {
        for (entry in entries)
            entry.name = entry.name.replace(fromField.text, toField.text)
        stage.close()
    }

    fun cancel() { stage.close() }
}