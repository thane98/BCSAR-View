package com.thane98.bcsarview.ui

import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.createErrorDialog
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.text.Font
import javafx.stage.Stage
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException

class Main : Application() {

    override fun start(stage: Stage) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            // Fix to avoid duplicate dialogs when a thread spawned by the JavaFX application thread dies.
            if (!(throwable is RuntimeException && throwable.cause is InvocationTargetException)) {
                Platform.runLater {
                    val dialog = createErrorDialog(throwable, "Error Occurred")
                    dialog.showAndWait()
                }
            }
        }

        Font.loadFont(this.javaClass.getResourceAsStream("DejaVuSans.ttf"), 14.0)
        Font.loadFont(this.javaClass.getResourceAsStream("DejaVuSansMono.ttf"), 14.0)
        val loader = FXMLLoader(this.javaClass.getResource("MainWindow.fxml"))
        val parent: Parent = loader.load()
        val scene = Scene(parent)
        applyStyles(scene)
        stage.scene = scene
        stage.title = "BCSAR View"
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}