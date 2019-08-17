package com.thane98.bcsarview.ui.forms

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception
import java.net.URL
import java.util.*

class ErrorDialogController(private val throwable: Throwable, private val headerText: String): Initializable {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var headerLabel: Label
    @FXML
    private lateinit var messageLabel: Label
    @FXML
    private lateinit var errorField: TextArea

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val errorText = sw.toString()
        errorField.text = errorText
        headerLabel.text = headerText
        messageLabel.text = throwable.message
        stage.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (event.code == KeyCode.ESCAPE)
                closeDialog()
        }
    }

    @FXML
    private fun closeDialog() { stage.close() }
}