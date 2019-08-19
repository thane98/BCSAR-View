package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.Configuration
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
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
    @FXML
    private lateinit var cwavToWavCommandField: TextField
    @FXML
    private lateinit var wavToCwavCommandField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        themeBox.items.addAll("Light", "Dark")
        loggingLevelBox.items.addAll(Level.OFF, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE)
        themeBox.selectionModel.select(Configuration.theme.value)
        loggingLevelBox.selectionModel.select(Configuration.loggingLevel.value)
        cwavToWavCommandField.text = Configuration.cwavToWavCommand.value
        wavToCwavCommandField.text = Configuration.wavToCwavCommand.value
    }

    @FXML
    private fun confirm() {
        Configuration.theme.value = themeBox.value
        Configuration.loggingLevel.value = loggingLevelBox.value
        Configuration.cwavToWavCommand.value = cwavToWavCommandField.text
        Configuration.wavToCwavCommand.value = wavToCwavCommandField.text
        Configuration.save()
        stage.close()
    }

    @FXML
    private fun cancel() { stage.close() }
}