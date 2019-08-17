package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.ui.Main
import com.thane98.bcsarview.ui.forms.ErrorDialogController
import javafx.fxml.FXMLLoader
import javafx.stage.FileChooser
import java.lang.Exception
import javafx.stage.Stage


fun createBcsarOpenDialog(): FileChooser {
    val dialog = FileChooser()
    dialog.title = "Open Sound Archive"
    dialog.extensionFilters.addAll(
        FileChooser.ExtensionFilter("3DS Sound Archives", "*.bcsar"),
        FileChooser.ExtensionFilter("All Files", "*.*")
    )
    return dialog
}

fun createErrorDialog(throwable: Throwable, headerText: String): Stage {
    val loader = FXMLLoader()
    loader.setController(ErrorDialogController(throwable, headerText))
    val stage = loader.load<Stage>(Main::class.java.getResourceAsStream("ErrorDialog.fxml"))
    applyStyles(stage.scene)
    return stage
}