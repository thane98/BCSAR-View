package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.Configuration
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import java.net.URL
import java.util.*
import java.util.logging.Level

class PreferencesController : Initializable {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var themeBox: ComboBox<String>
    @FXML
    private lateinit var loggingLevelBox: ComboBox<Level>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        themeBox.items.addAll("Light", "Dark")
        loggingLevelBox.items.addAll(Level.OFF, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE)
        themeBox.selectionModel.select(Configuration.theme.value)
        loggingLevelBox.selectionModel.select(Configuration.loggingLevel.value)
    }

    @FXML
    private fun confirm() {
        Configuration.theme.value = themeBox.value
        Configuration.loggingLevel.value = loggingLevelBox.value
        Configuration.save()
        stage.close()
    }

    @FXML
    private fun cancel() { stage.close() }
}