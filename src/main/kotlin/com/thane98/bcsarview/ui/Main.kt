package com.thane98.bcsarview.ui

import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.text.Font
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
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