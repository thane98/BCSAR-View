package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.ui.BCSARView
import com.thane98.bcsarview.ui.forms.ErrorDialogController
import javafx.fxml.FXMLLoader
import javafx.stage.FileChooser
import javafx.stage.Stage

class Dialogs {
    companion object {
        val bcsarChooser = FileChooser()

        init {
            bcsarChooser.title = "Select a sound archive."
            bcsarChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("3DS Sound Archives", "*.bcsar"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )
        }
    }
}

fun createErrorDialog(throwable: Throwable, headerText: String): Stage {
    val loader = FXMLLoader()
    loader.setController(ErrorDialogController(throwable, headerText))
    val stage = loader.load<Stage>(BCSARView::class.java.getResourceAsStream("ErrorDialog.fxml"))
    applyStyles(stage.scene)
    return stage
}