package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.ui.Main
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import jfxtras.styles.jmetro8.JMetro

fun applyStyles(scene: Scene) {
    scene.stylesheets.clear()
    scene.stylesheets.add(Main::class.java.getResource("styles-common.css").toExternalForm())
    if (Configuration.theme.value == "Light") {
        JMetro(JMetro.Style.LIGHT).applyTheme(scene)
    } else {
        JMetro(JMetro.Style.DARK).applyTheme(scene)
        scene.stylesheets.add(Main::class.java.getResource("styles-dark.css").toExternalForm())
    }
}

fun loadAndShowForm(formName: String) {
    val loader = FXMLLoader()
    val stage = loader.load<Stage>(Main::class.java.getResourceAsStream(formName))
    applyStyles(stage.scene)
    stage.showAndWait()
}

fun loadAndShowForm(formName: String, controller: Any) {
    val loader = FXMLLoader()
    loader.setController(controller)
    val stage = loader.load<Stage>(Main::class.java.getResourceAsStream(formName))
    applyStyles(stage.scene)
    stage.showAndWait()
}