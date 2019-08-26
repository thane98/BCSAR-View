package com.thane98.bcsarview.ui

import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.createErrorDialog
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.text.Font
import javafx.stage.Stage
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import java.text.ParseException

class Main : Application() {
    // Suppressing two kinds of exceptions here
    // - InvocationTargetExceptions, will already catch the nested exception in this case.
    // - ParseException, thrown by NumberStringConverter when user enters bad input. Not worth a dialog.
    private fun needToNotifyUser(throwable: Throwable): Boolean {
        return !(throwable is RuntimeException && (throwable.cause is InvocationTargetException || throwable.cause is ParseException))
    }

    override fun start(stage: Stage) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            // Fix to avoid duplicate dialogs when a thread spawned by the JavaFX application thread dies.
            if (needToNotifyUser(throwable)) {
                Platform.runLater {
                    val dialog = createErrorDialog(throwable, "Error Occurred")
                    dialog.showAndWait()
                }
            }
            throwable.printStackTrace()
        }

        Font.loadFont(this.javaClass.getResourceAsStream("DejaVuSans.ttf"), 14.0)
        Font.loadFont(this.javaClass.getResourceAsStream("DejaVuSansMono.ttf"), 14.0)
        val loader = FXMLLoader(this.javaClass.getResource("MainWindow.fxml"))
        val parent: Parent = loader.load()
        val controller = loader.getController<MainWindowController>()
        val scene = Scene(parent)
        applyStyles(scene)
        stage.scene = scene
        stage.title = "BCSAR View"
        stage.setOnCloseRequest { controller.shutdown() }
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}