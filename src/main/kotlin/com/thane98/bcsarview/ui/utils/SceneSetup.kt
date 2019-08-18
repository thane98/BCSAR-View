package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.ui.Main
import javafx.scene.Scene
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