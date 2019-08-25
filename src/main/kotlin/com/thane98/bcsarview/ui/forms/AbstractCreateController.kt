package com.thane98.bcsarview.ui.forms

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
    protected abstract fun create()

    @FXML
    protected fun cancel() { stage.close() }
}