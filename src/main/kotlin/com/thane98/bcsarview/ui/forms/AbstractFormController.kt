package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.ui.Main
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.layout.StackPane
import kotlin.concurrent.thread

abstract class AbstractFormController : Initializable {
    @FXML
    private lateinit var container: StackPane
    private val waitingIndicator: StackPane = FXMLLoader.load(Main::class.java.getResource("WaitingIndicator.fxml"))

    fun performWithWaitingScreen(action: () -> Unit) {
        showWaitingScreen()
        thread {
            try {
                action()
            } finally {
                Platform.runLater { hideWaitingScreen() }
            }
        }
    }

    private fun showWaitingScreen() { container.children.add(waitingIndicator) }
    private fun hideWaitingScreen() { container.children.remove(waitingIndicator) }
}