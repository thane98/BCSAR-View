package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.ui.BCSARView
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

fun applyStyles(_scene: Scene) {

}

fun loadAndShowForm(formName: String) {
    val loader = FXMLLoader()
    val stage = loader.load<Stage>(BCSARView::class.java.getResourceAsStream(formName))
    applyStyles(stage.scene)
    stage.showAndWait()
}

fun loadAndShowForm(formName: String, controller: Any) {
    val loader = FXMLLoader()
    loader.setController(controller)
    val stage = loader.load<Stage>(BCSARView::class.java.getResourceAsStream(formName))
    applyStyles(stage.scene)
    stage.showAndWait()
}