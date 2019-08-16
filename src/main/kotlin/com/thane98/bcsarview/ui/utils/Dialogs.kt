package com.thane98.bcsarview.ui.utils

import javafx.stage.FileChooser

fun createBcsarOpenDialog(): FileChooser {
    val dialog = FileChooser()
    dialog.title = "Open Sound Archive"
    dialog.extensionFilters.addAll(
        FileChooser.ExtensionFilter("3DS Sound Archives", "*.bcsar"),
        FileChooser.ExtensionFilter("All Files", "*.*")
    )
    return dialog
}