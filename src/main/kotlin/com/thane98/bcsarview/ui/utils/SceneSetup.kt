package com.thane98.bcsarview.ui.utils

import com.thane98.bcsarview.ui.Main
import javafx.scene.Scene
import jfxtras.styles.jmetro8.JMetro

fun applyStyles(scene: Scene) {
    JMetro(JMetro.Style.DARK).applyTheme(scene)
    scene.stylesheets.add(Main::class.java.getResource("styles-common.css").toExternalForm())
    scene.stylesheets.add(Main::class.java.getResource("styles-dark.css").toExternalForm())
}